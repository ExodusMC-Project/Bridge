package net.pixteria.bridge;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bridge {

    private static Bridge instance;

    private final String defaultTopicName;
    private final RedissonClient redissonClient;
    private final String instanceId;
    private final BridgeEventPipeline pipeline;
    private boolean closed = false;

    public Bridge(String defaultTopicName, String instanceId, RedissonClient redissonClient) {
        if (instance != null) {
            throw new IllegalStateException("Bridge is already initialized");
        }
        instance = this;
        this.defaultTopicName = defaultTopicName;
        this.instanceId = instanceId;
        this.redissonClient = redissonClient;
        this.pipeline = new BridgeEventPipeline(this, this.redissonClient);
    }

    public boolean isClosed() {
        return this.closed;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public BridgeEventPipeline getPipeline() {
        return this.pipeline;
    }

    public String getDefaultTopicName() {
        return this.defaultTopicName;
    }

    public static Bridge getInstance() {
        return instance;
    }
}
