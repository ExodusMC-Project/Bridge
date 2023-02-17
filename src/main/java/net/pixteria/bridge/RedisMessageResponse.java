package net.pixteria.bridge;

public final class RedisMessageResponse extends RedisMessage {

    private final RedisMessage request;

    private final Object data;

    public RedisMessageResponse(final RedisMessage request, final Object data) {
        this.request = request;
        this.data = data;
    }

    RedisMessage request() {
        return this.request;
    }

    Object data() {
        return this.data;
    }
}
