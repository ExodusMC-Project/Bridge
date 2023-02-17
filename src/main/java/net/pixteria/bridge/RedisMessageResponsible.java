package net.pixteria.bridge;

import java.util.UUID;

public abstract class RedisMessageResponsible<T> extends RedisMessage {

    private Pipeline pipeline;

    private String topic;

    void init(final UUID id, final String instanceId, final String target, final Pipeline pipeline, final String topic) {
        this.init(id, instanceId, target);
        this.pipeline = pipeline;
        this.topic = topic;
    }

    public void reply(final T value) {
        this.pipeline.callAndForget(this.topic, new Response(this, value));
    }
}
