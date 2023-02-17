package net.pixteria.bridge;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class BridgeRedis implements BridgePubSub {

    private final Map<String, RTopic> topics = new ConcurrentHashMap<>();

    private final RedissonClient redis;

    public BridgeRedis(final RedissonClient redis) {
        this.redis = redis;
    }

    @Override
    public <T extends BridgeMessage> void subscribe(String topic, Class<T> cls, Consumer<T> consumer) {
        this.topic(topic).addListener(cls, (channel, msg) -> consumer.accept(msg));
    }

    @Override
    public void publish(final String topic, final BridgeMessage message) {
        this.topic(topic).publish(message);
    }

    private RTopic topic(final String topic) {
        return this.topics.computeIfAbsent(topic, this.redis::getTopic);
    }
}
