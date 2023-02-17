package net.pixteria.bridge.future;

public final class Response extends Event {

    private final Event request;

    private final Object data;

    public Response(final Event request, final Object data) {
        this.request = request;
        this.data = data;
    }

    public Event request() {
        return request;
    }

    public Object data() {
        return data;
    }
}
