package net.pixteria.bridge;

import java.util.function.Consumer;

public interface BridgePubSub {

    <T extends BridgeMessage> void subscribe(String topic, Class<T> cls, Consumer<T> consumer);

    void publish(String topic, BridgeMessage message);
}
