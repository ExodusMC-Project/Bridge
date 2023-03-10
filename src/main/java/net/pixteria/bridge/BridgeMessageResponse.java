package net.pixteria.bridge;

import java.util.UUID;

public class BridgeMessageResponse extends BridgeMessage {

    private UUID parentId;
    private Object data;

    public BridgeMessageResponse() {

    }

    public BridgeMessageResponse(final BridgeMessage request, final Object data) {
        this.parentId = request.getRequestId();
        this.data = data;
    }

    public UUID getParentId() {
        return this.parentId;
    }

    public Object getData() {
        return this.data;
    }
}
