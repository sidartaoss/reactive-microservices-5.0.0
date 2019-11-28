package io.vertx.microservice;

import io.vertx.core.AbstractVerticle;

/**
 * Exercise2Verticle
 */
public class Exercise2Verticle extends AbstractVerticle {

    @Override
    public void start() {
        // You can acces the Vertx.x instance you deployed on using the `vertx` (inherited) field

        // Create a Http server
        // Instead of "hello", display the name of the thread serving the request (using Thread.currentThread().getName())
        vertx.createHttpServer()
            .requestHandler(request -> {
                sleep();
                request.response().end(Thread.currentThread().getName());
            })
            .listen(8080);
    }


    /**
     * Method used in the Exercise 3
     * 
     */
    private void sleep() {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            // Ignore me
        }
    }
}