package net.pixteria.bridge;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.function.Consumer;

public final class BridgeRedis implements BridgePubSub {

    private final RTopic topic;

    public BridgeRedis(final RedissonClient redis, final String topic) {
        this.topic = redis.getTopic(topic);
    }

    @Override
    public <T extends BridgeMessage> void subscribe(final Class<T> cls, final Consumer<T> consumer) {
        this.topic.addListener(cls, (channel, msg) -> consumer.accept(msg));
    }

    @Override
    public void publish(final BridgeMessage message) {
        this.topic.publish(message);
    }
}
