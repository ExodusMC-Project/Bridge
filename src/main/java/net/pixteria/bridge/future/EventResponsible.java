package net.pixteria.bridge.future;

public abstract class EventResponsible<T> extends Event {

    private Pipeline pipeline;

    private String topic;

    void init(final Pipeline pipeline, final String topic) {
        this.pipeline = pipeline;
        this.topic = topic;
    }

    public void reply(final T value) {
        this.pipeline.callAndForget(this.topic, new Response<>(this, value));
    }
}
