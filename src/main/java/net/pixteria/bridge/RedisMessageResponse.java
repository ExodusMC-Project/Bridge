package net.pixteria.bridge;

public final class RedisMessageResponse<T extends RedisMessage> extends RedisMessage {

    private final RedisMessageResponsible<T> request;

    private final T data;

    public RedisMessageResponse(final RedisMessageResponsible<T> request, final T data) {
        this.request = request;
        this.data = data;
    }

    RedisMessageResponsible<T> request() {
        return this.request;
    }

    T data() {
        return this.data;
    }
}
