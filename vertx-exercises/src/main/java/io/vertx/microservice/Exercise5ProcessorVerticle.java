package io.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 *  A verticle replying to incoming messages
 * 
 */
public class Exercise5ProcessorVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        
        EventBus eventBus = vertx.eventBus();

        // Register a consumer and call the `reply` method with a JSON object containing the greetings 
        // message. ~ parameter is passed in the incoming message body (a name). For example, if the 
        // incoming message is the String "vert.x", the reply contains: `{"message":"hello vert.x"}`.
        // Unlike the previous exercise, the incoming message has a `String` body.
        eventBus.consumer("greetings", msg -> {
            JsonObject json = new JsonObject().put("message", "hello " + msg.body());
            msg.reply(json);
        });


    }
}