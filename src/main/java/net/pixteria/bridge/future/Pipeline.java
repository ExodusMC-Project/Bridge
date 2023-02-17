package net.pixteria.bridge.future;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Pipeline {

    private final Map<String, RTopic> topics = new ConcurrentHashMap<>();

    private final Map<Event, CompletableFuture> responses = new ConcurrentHashMap<>();

    private final RedissonClient redis;

    public Pipeline(final RedissonClient redis) {
        this.redis = redis;
    }

    public <T extends Event> void register(final String topic, final Class<T> cls, final Consumer<T> consumer) {
        this.topics.computeIfAbsent(topic, redis::getTopic).addListener(cls, (channel, msg) -> consumer.accept(msg));
        if (cls.isAssignableFrom(EventResponsible.class)) {
            this.topics.computeIfAbsent(topic, redis::getTopic).addListener(Response.class, (channel, msg) -> {
                final var future = this.responses.get(msg.request());
                if (future != null) {
                    future.complete(msg.data());
                }
            });
        }
    }

    public <T extends Event> void callAndForget(final String topic, final T event) {
        this.topics.computeIfAbsent(topic, redis::getTopic).publish(event);
        if (event instanceof EventResponsible<?>) {
            ((EventResponsible<?>) event).init(this, topic);
        }
    }

    public <R, T extends EventResponsible<R>> CompletableFuture<R> call(final String topic, final T data, final Duration timeout) {
        this.callAndForget(topic, data);
        final var future = new CompletableFuture<R>();
        if (!timeout.isNegative()) {
            future
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((__, t) -> this.responses.remove(data));
        }
        this.responses.put(data, future);
        return future;
    }
}
