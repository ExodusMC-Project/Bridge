package net.pixteria.bridge;

public abstract class RedisMessageResponsible<T> extends RedisMessage {

    private RedisPipeline pipeline;

    private String topic;

    void init(final RedisPipeline redisPipeline, final String topic) {
        this.pipeline = redisPipeline;
        this.topic = topic;
    }

    public void reply(final T value) {
        this.pipeline.callAndForget(this.instanceId(), this.topic, new RedisMessageResponse(this, value));
    }
}
