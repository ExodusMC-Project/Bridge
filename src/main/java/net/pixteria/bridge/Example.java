package net.pixteria.bridge;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.time.Duration;

final class Example {

    public static void main(String[] args) {
        final RedissonClient redis = Redisson.create();
        final String instanceId = "skyblock-1";
        final BridgePipeline pipeline = new BridgePipeline(new BridgeRedis(redis, "test-message"), instanceId);

        pipeline.register(TestMessage.Request.class, message -> {
            message.reply(new TestMessage.Response("pong"));
        });

        pipeline.call(new TestMessage.Request("ping"), Duration.ofSeconds(2L)).thenAccept(response -> System.out.println(response.test2));
    }

    private static class TestMessage {
        private static final class Request extends BridgeMessageResponsible<Response> {
            private String test;

            public Request(String test) {
                this.test = test;
            }
        }

        private static class Response {
            private String test2;

            public Response(String test2) {
                this.test2 = test2;
            }
        }
    }
}
