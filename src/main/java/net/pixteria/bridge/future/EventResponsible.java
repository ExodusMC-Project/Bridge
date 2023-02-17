package net.pixteria.bridge.future;

import java.util.UUID;

public abstract class EventResponsible<T> extends Event {

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
