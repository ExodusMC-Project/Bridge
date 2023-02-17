package net.pixteria.bridge;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class RedisPipeline {

    private final Map<String, RTopic> topics = new ConcurrentHashMap<>();

    private final Map<RedisMessage, CompletableFuture> responses = new ConcurrentHashMap<>();

    private final RedissonClient redis;

    private final String instanceId;

    private Predicate<RedisMessage> filter;

    public RedisPipeline(final RedissonClient redis, final String instanceId) {
        this.redis = redis;
        this.instanceId = instanceId;
    }

    public RedisPipeline filter(final Predicate<RedisMessage> filter) {
        if (this.filter == null) {
            this.filter = redisMessage -> true;
        }
        this.filter = this.filter.and(filter);
        return this;
    }

    public <T extends RedisMessage> void register(final String topic, final Class<T> cls, final Consumer<T> consumer) {
        this.register(topic, cls, true, consumer);
    }

    public <T extends RedisMessage> void register(final String topic, final Class<T> cls, final boolean acceptsItself, final Consumer<T> consumer) {
        this.topic(topic).addListener(cls, (channel, msg) -> {
            if (msg.target() != null && !this.instanceId.equals(msg.target())) {
                return;
            }
            if (msg.instanceId().equals(this.instanceId) && !acceptsItself) {
                return;
            }
            if (this.filter != null && !this.filter.test(msg)) {
                return;
            }
            consumer.accept(msg);
        });
        if (RedisMessageResponsible.class.isAssignableFrom(cls)) {
            this.register(topic, RedisMessageResponse.class, acceptsItself, response -> {
                final var future = this.responses.get(response.request());
                if (future != null) {
                    future.complete(response.data());
                }
            });
        }
    }

    public <T extends RedisMessage> void callAndForget(final String topic, final T message) {
        this.callAndForget(null, topic, message);
    }

    public <T extends RedisMessage> void callAndForget(final String target, final String topic, final T message) {
        this.topic(topic).publish(message);
        message.init(UUID.randomUUID(), this.instanceId, target);
        if (message instanceof RedisMessageResponsible<?> responsible) {
            responsible.init(this, topic);
        }
    }

    public <R, T extends RedisMessageResponsible<R>> CompletableFuture<R> call(final String topic, final T data, final Duration timeout) {
        return this.call(null, topic, data, timeout);
    }

    public <R, T extends RedisMessageResponsible<R>> CompletableFuture<R> call(final String target, final String topic, final T data, final Duration timeout) {
        this.callAndForget(target, topic, data);
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
        return this.topics.computeIfAbsent(topic, this.redis::getTopic);
    }
}
