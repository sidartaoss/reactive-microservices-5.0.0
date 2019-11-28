package io.vertx.microservice;


import io.vertx.core.Vertx;

/**
 *      * Create the Vert.x instance
 * 
 *      * Create a Http server 
 */
public class Exercise1 {


    public static void main(String[] args) {
        
        // 1. Create the Vert.x instance using Vertx.vertx (use io.vertx.core.Vertx)
        Vertx vertx = Vertx.vertx();

        // 2. Create a Http Server using the `createHttpServer` method. Set a request handler doing:
        // `req.response().end("hello")
        // Call the listen method with `8080` as parameter
        vertx.createHttpServer()
            .requestHandler(request -> request.response().end("hello"))
            .listen(8080);

    }
    
}