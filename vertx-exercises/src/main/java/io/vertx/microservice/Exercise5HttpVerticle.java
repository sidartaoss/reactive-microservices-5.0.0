package io.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * A verticle using the request-reply event bus delivery mechanism to handle Http requests. 
 * 
 * 
 */
public class Exercise5HttpVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        
        vertx.createHttpServer()
                .requestHandler(request -> {

                    // 1 - Retrieve the `name` (query) parameter. Set it to `world` if null. 
                    // You can retrieve the parameter using `request.getParam()`.
                    String name = request.getParam("name");
                    if (name == null) name = "world";

                    // 2 - Send a message on the event bus using the `send` method.
                    // Pass a reply handler receiving the response. As the expected object is a Json
                    // structure, you can use `vertx.eventBus`.<JsonObject>send(...).
                    // In the reply handler, you receive an `AsyncResult`. This structure describes the
                    // outcome from an asynchronous operation: a success (and a result) or a failure 
                    // (and a cause). If it is a failure (check with the `failed` method), write a 500 Http 
                    // response with the cause (`cause.getMessage()`) as payload. On success, write the body
                    // into the Http response.
                    vertx.eventBus().<JsonObject>send("greetings", name, reply -> {
                        if (reply.failed()) {
                            request.response().setStatusCode(500).end(reply.cause().getMessage());
                        } else {
                            request.response().end(reply.result().body().encode());
                        }
                    });
                })
                .listen(8080);

    }
}