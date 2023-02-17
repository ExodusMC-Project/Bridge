package net.pixteria.bridge;

public final class Response extends RedisMessage {

    private final RedisMessage request;

    private final Object data;

    public Response(final RedisMessage request, final Object data) {
        this.request = request;
        this.data = data;
    }

    public RedisMessage request() {
        return this.request;
    }

    public Object data() {
        return this.data;
    }
}
