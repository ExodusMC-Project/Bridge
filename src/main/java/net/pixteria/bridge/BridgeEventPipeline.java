package net.pixteria.bridge;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BridgeEventPipeline {

    private final Bridge bridge;
    private final RedissonClient redissonClient;
    private final Map<String, RTopic> topicMap;

    private final Set<Integer> responseQueue;
    private final Map<Integer, BridgeResponse.BridgeResponseEvent<?>> responses;

    public BridgeEventPipeline(Bridge bridge, RedissonClient redissonClient) {
        this.bridge = bridge;
        this.redissonClient = redissonClient;
        this.topicMap = new ConcurrentHashMap<>();
        this.responseQueue = ConcurrentHashMap.newKeySet();
        this.responses = new ConcurrentHashMap<>();

        this.registerListener(bridge.getInstanceId(), BridgeResponse.BridgeResponseEvent.class, bridgeResponseEvent -> {
            if (!this.responseQueue.contains(bridgeResponseEvent.getEventId())) {
                return;
            }
            this.responses.put(bridgeResponseEvent.getEventId(), bridgeResponseEvent);
            this.responseQueue.remove(bridgeResponseEvent.getEventId());
        });
    }

    public <M extends BridgeEvent> void registerListener(String topicName, Class<M> clazz, Consumer<M> consumer) {
        this.topicMap.computeIfAbsent(topicName, this.redissonClient::getTopic).addListener(clazz, (channel, msg) -> consumer.accept(msg));
    }

    public <M extends BridgeEvent> void callEvent(String topicName, M event) {
        if (this.bridge.isClosed()) {
            throw new IllegalStateException("Attempted to call an event while Bridge was closed.");
        }
        this.topicMap.computeIfAbsent(topicName, this.redissonClient::getTopic).publish(event);
    }

    public <R, M extends BridgeEvent> BridgeResponse<R> callEventAndWait(String topicName, M event, long timeout) {
        long startTime = System.currentTimeMillis();

        this.responseQueue.add(event.getEventId());
        boolean expired = false;

        while (!expired && this.responseQueue.contains(event.getEventId())) {
            expired = System.currentTimeMillis() - startTime > timeout;
        }

        if (expired) {
            this.responses.remove(event.getEventId());
            return null;
        }

        return new BridgeResponse<R>((BridgeResponse.BridgeResponseEvent<R>) this.responses.remove(event.getEventId()));
    }
}
