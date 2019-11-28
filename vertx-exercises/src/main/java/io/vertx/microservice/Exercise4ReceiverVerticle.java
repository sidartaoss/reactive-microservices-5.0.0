package io.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Exercise4ReceiverVerticle
 */
public class Exercise4ReceiverVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        
        // Retrieve the event bus and register a consumer on the "greetings" address. For each message,
        // print it on the console. You can retrieve the message body using `body()`. Use the method 
        // `encodePrettily` on the retrieved Json body to print it nicely.

        vertx.eventBus().<JsonObject>consumer("greetings", msg -> {
            System.out.println(msg.body().encodePrettily());
        });

    }
}