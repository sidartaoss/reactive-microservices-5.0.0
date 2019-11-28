package io.vertx.microservice;


import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;

/**
 * A verticle using the request-reply event bus delivery mechanism to handle Http requests
 * 
 * 
 */
public class Exercise6HttpVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        

        vertx.createHttpServer()
            .requestHandler(request -> {
                
                String name = request.getParam("name");
                if (name == null) name = "world";


                // Send a message on the event bus using the `send` method. Pass a reply handler 
                // receiving the response. As the expected object is a Json structure, you can use 
                // `vertx.eventBus().<JsonObject>send(...)`
                // Unlike in the previous exercise, we use the `rxSend` method to retrieve a `Single` stream. 
                // We then map the result 
                // to extract the (encoded as String) Json structure.
                // In RX, we must `subscribe` to the stream to trigger the processing. Without nothing happens. 
                // There are several 
                // `subscribe` method, but here we recommend the `BiConsumer` format `(res, err) -> ... `
                // If it's a failure (err != null), write a 500 Http response with the cause (`err.getMessage()`) 
                // as payload. On
                // success, write the body (`res`) into the Http response.  
                vertx.eventBus().<JsonObject>rxSend("greetings", name)
                    .map(message -> message.body().encode())
                    .subscribe((res, err) -> {
                        if (err != null) {
                            request.response().setStatusCode(500).end(err.getMessage());
                        } else {
                            request.response().end(res);
                        }
                    });
                })
                .listen(8080);
        

    }
}