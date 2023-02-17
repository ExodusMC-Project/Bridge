package net.pixteria.bridge;

public abstract class BridgeMessageResponsible<T> extends BridgeMessage {

    private BridgePipeline pipeline;

    private String topic;

    void init(final BridgePipeline pipeline, final String topic) {
        this.pipeline = pipeline;
        this.topic = topic;
    }

    public void reply(final T value) {
        this.pipeline.callAndForget(this.instanceId(), this.topic, new BridgeMessageResponse(this, value));
    }
}
