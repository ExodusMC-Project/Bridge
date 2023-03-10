package net.pixteria.bridge;

import java.util.Objects;
import java.util.UUID;

public abstract class BridgeMessage {

    private UUID id;

    private String instanceId;

    private String target;

    private boolean initiated = false;

    void init(final UUID id, final String instanceId, final String target) {
        if (this.initiated) {
            return;
        }
        this.id = id;
        this.instanceId = instanceId;
        this.target = target;
        this.initiated = true;
    }

    String instanceId() {
        return this.instanceId;
    }

    String target() {
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
        final var message = (BridgeMessage) o;
        return Objects.equals(this.id, message.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
