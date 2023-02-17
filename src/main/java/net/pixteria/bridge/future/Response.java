package net.pixteria.bridge.future;

public final class Response<R> extends Event {

    private final Event request;

    private final R data;

    public Response(final Event request, final R data) {
        this.request = request;
        this.data = data;
    }

    public Event request() {
        return request;
    }

    public R data() {
        return data;
    }
}
