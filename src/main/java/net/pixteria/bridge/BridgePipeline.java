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

    private final Map<UUID, CompletableFuture> responses = new ConcurrentHashMap<>();

    private final BridgePubSub pubSub;

    private final String instanceId;

    private Predicate<BridgeMessage> filter;

    public BridgePipeline(final BridgePubSub pubSub, final String instanceId) {
        this.pubSub = pubSub;
        this.instanceId = instanceId;
        this.register(BridgeMessageResponse.class, response -> {
            final CompletableFuture future = this.responses.get(response.getParentId());
            if (future != null) {
                future.complete(response.getData());
            }
        });
    }

    public BridgePipeline filter(final Predicate<BridgeMessage> filter) {
        if (this.filter == null) {
            this.filter = message -> true;
        }
        this.filter = this.filter.and(filter);
        return this;
    }

    public <T extends BridgeMessage> void register(final Class<T> cls, final Consumer<T> consumer) {
        this.register(cls, true, consumer);
    }

    public <T extends BridgeMessage> void register(final Class<T> cls, final boolean acceptsItself, final Consumer<T> consumer) {
        this.pubSub.subscribe(cls, (msg) -> {
            if (msg.getTarget() != null && !this.instanceId.equals(msg.getTarget())) {
                return;
            }
            if (msg.getInstanceId().equals(this.instanceId) && !acceptsItself) {
                return;
            }
            if (this.filter != null && !this.filter.test(msg)) {
                return;
            }
            if (msg instanceof BridgeMessageResponsible<?> responsible) {
                responsible.init(this);
            }
            consumer.accept(msg);
        });
    }

    public <T extends BridgeMessage> void callAndForget(final T message) {
        this.callAndForget(null, message);
    }

    public <T extends BridgeMessage> void callAndForget(final String target, final T message) {
        message.init(UUID.randomUUID(), this.instanceId, target);
        if (message instanceof BridgeMessageResponsible<?> responsible) {
            responsible.init(this);
        }
        this.pubSub.publish(message);
    }

    public <R, T extends BridgeMessageResponsible<R>> CompletableFuture<R> call(final T data, final Duration timeout) {
        return this.call(null, data, timeout);
    }

    public <R, T extends BridgeMessageResponsible<R>> CompletableFuture<R> call(final String target, final T data, final Duration timeout) {
        final CompletableFuture<R> future = new CompletableFuture<R>();
        if (!timeout.isNegative()) {
            future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        data.init(UUID.randomUUID(), this.instanceId, target);
        data.init(this);
        future.whenComplete((__, t) -> this.responses.remove(data.getRequestId()));
        this.responses.put(data.getRequestId(), future);
        this.pubSub.publish(data);
        return future;
    }
}
