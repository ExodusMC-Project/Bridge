package net.pixteria.bridge;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BridgePipeline {

    private final Map<BridgeMessage, CompletableFuture> responses = new ConcurrentHashMap<>();

    private final BridgePubSub pubSub;

    private final String instanceId;

    private Predicate<BridgeMessage> filter;

    public BridgePipeline(final BridgePubSub pubSub, final String instanceId) {
        this.pubSub = pubSub;
        this.instanceId = instanceId;
    }

    public BridgePipeline filter(final Predicate<BridgeMessage> filter) {
        if (this.filter == null) {
            this.filter = message -> true;
        }
        this.filter = this.filter.and(filter);
        return this;
    }

    public <T extends BridgeMessage> void register(final String topic, final Class<T> cls, final Consumer<T> consumer) {
        this.register(topic, cls, true, consumer);
    }

    public <T extends BridgeMessage> void register(final String topic, final Class<T> cls, final boolean acceptsItself, final Consumer<T> consumer) {
        this.pubSub.subscribe(topic, cls, (msg) -> {
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
        if (BridgeMessageResponsible.class.isAssignableFrom(cls)) {
            this.register(topic, BridgeMessageResponse.class, acceptsItself, response -> {
                final var future = this.responses.get(response.request());
                if (future != null) {
                    future.complete(response.data());
                }
            });
        }
    }

    public <T extends BridgeMessage> void callAndForget(final String topic, final T message) {
        this.callAndForget(null, topic, message);
    }

    public <T extends BridgeMessage> void callAndForget(final String target, final String topic, final T message) {
        message.init(UUID.randomUUID(), this.instanceId, target);
        if (message instanceof BridgeMessageResponsible<?> responsible) {
            responsible.init(this, topic);
        }
        this.pubSub.publish(topic, message);
    }

    public <R, T extends BridgeMessageResponsible<R>> CompletableFuture<R> call(final String topic, final T data, final Duration timeout) {
        return this.call(null, topic, data, timeout);
    }

    public <R, T extends BridgeMessageResponsible<R>> CompletableFuture<R> call(final String target, final String topic, final T data, final Duration timeout) {
        final var future = new CompletableFuture<R>();
        if (!timeout.isNegative()) {
            future
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((__, t) -> this.responses.remove(data));
        }
        this.responses.put(data, future);
        this.callAndForget(target, topic, data);
        return future;
    }
}
