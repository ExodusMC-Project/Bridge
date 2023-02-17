package net.pixteria.bridge.future;

final class EventRaw<T> extends Event {

    private final T data;

    EventRaw(final T data) {
        this.data = data;
    }

    public T data() {
        return this.data;
    }
}
