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

    private final String instanceId;

    public Pipeline(final RedissonClient redis, final String instanceId) {
        this.redis = redis;
        this.instanceId = instanceId;
    }

    public <T> void register(final String topic, final Class<T> cls, final Consumer<T> consumer) {
        this.topic(topic).addListener(cls, (channel, msg) -> consumer.accept(msg));
        if (EventResponsible.class.isAssignableFrom(cls)) {
            this.register(topic, Response.class, response -> {
                final var future = this.responses.get(response.request());
                if (future != null) {
                    future.complete(response.data());
                }
            });
        }
    }

    public <T> void callAndForget(final String topic, final T event) {
        this.topic(topic).publish(event);
        if (event instanceof EventResponsible<?> responsible) {
            responsible.init(this.instanceId, this, topic);
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

    private RTopic topic(final String topic) {
        return this.topics.computeIfAbsent(topic, redis::getTopic);
    }
}
