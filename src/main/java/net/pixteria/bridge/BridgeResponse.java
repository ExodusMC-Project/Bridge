package net.pixteria.bridge;

public class BridgeResponse<R> {

    private final int eventId;
    private final String respondingInstance;
    private final R response;

    protected BridgeResponse(BridgeResponseEvent<R> responseEvent) {
        this.eventId = responseEvent.getEventId();
        this.respondingInstance = responseEvent.getRespondingInstance();
        this.response = responseEvent.getResponse();
    }

    public int getEventId() {
        return this.eventId;
    }

    public String getRespondingInstance() {
        return this.respondingInstance;
    }

    public R getResponse() {
        return this.response;
    }

    protected static class BridgeResponseEvent<R> extends BridgeEvent {

        private final int eventId;
        private final String respondingInstance;
        private final R response;

        public BridgeResponseEvent(int eventId, String respondingInstance, R response) {
            this.eventId = eventId;
            this.respondingInstance = respondingInstance;
            this.response = response;
        }

        public int getEventId() {
            return this.eventId;
        }

        public String getRespondingInstance() {
            return this.respondingInstance;
        }

        public R getResponse() {
            return this.response;
        }
    }
}
