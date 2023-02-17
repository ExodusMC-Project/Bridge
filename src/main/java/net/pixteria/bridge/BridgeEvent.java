package net.pixteria.bridge;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BridgeEvent {

    private final AtomicInteger EVENT_COUNTER = new AtomicInteger();

    private final int eventId;
    private final String ownerInstance;

    public BridgeEvent() {
        this.eventId = EVENT_COUNTER.getAndIncrement();
        this.ownerInstance = Bridge.getInstance().getInstanceId();
    }

    public int getEventId() {
        return this.eventId;
    }

    public String getOwnerInstance() {
        return this.ownerInstance;
    }

    public <R> void reply(R value) {
        new BridgeResponse.BridgeResponseEvent<>(this.eventId, Bridge.getInstance().getInstanceId(), value).call();
    }

    public void call() {
        this.call(Bridge.getInstance().getDefaultTopicName());
    }

    public CompletableFuture<Void> callAsync() {
        return CompletableFuture.runAsync(this::call);
    }

    public void call(String topicName) {
        Bridge.getInstance().getPipeline().callEvent(topicName, this);
    }

    public <R> BridgeResponse<R> callAndWait() {
        return this.callAndWait(Bridge.getInstance().getDefaultTopicName());
    }

    public <R> BridgeResponse<R> callAndWait(String topicName) {
        return this.callAndWait(topicName, TimeUnit.SECONDS.toMillis(10));
    }

    public <R> BridgeResponse<R> callAndWait(String topicName, long timeout) {
        return Bridge.getInstance().getPipeline().callEventAndWait(topicName, this, timeout);
    }
}
