package net.pixteria.bridge;

import java.util.Objects;
import java.util.UUID;

public abstract class BridgeMessage {

    private UUID id;
    private String instanceId;
    private String target;

    void init(final UUID id, final String instanceId, final String target) {
        this.id = id;
        this.instanceId = instanceId;
        this.target = target;
    }

    protected UUID getRequestId() {
        return this.id;
    }

    protected String getInstanceId() {
        return this.instanceId;
    }

    protected String getTarget() {
        return this.target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BridgeMessage message = (BridgeMessage) o;
        return Objects.equals(this.id, message.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
