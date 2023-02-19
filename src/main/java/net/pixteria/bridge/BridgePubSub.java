package net.pixteria.bridge;

import java.util.function.Consumer;

public interface BridgePubSub {

    <T extends BridgeMessage> void subscribe(Class<T> cls, Consumer<T> consumer);

    void publish(BridgeMessage message);
}
