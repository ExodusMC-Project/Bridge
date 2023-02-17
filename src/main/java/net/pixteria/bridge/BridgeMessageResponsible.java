package net.pixteria.bridge;

public abstract class BridgeMessageResponsible<T> extends BridgeMessage {

    private BridgePipeline pipeline;

    void init(final BridgePipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void reply(final T value) {
        this.pipeline.callAndForget(this.instanceId(), new BridgeMessageResponse(this, value));
    }
}
