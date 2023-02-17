package net.pixteria.bridge;

public final class BridgeMessageResponse extends BridgeMessage {

    private final BridgeMessage request;

    private final Object data;

    public BridgeMessageResponse(final BridgeMessage request, final Object data) {
        this.request = request;
        this.data = data;
    }

    BridgeMessage request() {
        return this.request;
    }

    Object data() {
        return this.data;
    }
}
