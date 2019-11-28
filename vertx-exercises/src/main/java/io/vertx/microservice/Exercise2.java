package io.vertx.microservice;

import io.vertx.core.Vertx;

/**
 * Create the Http server in the Exercise2Verticle
 * Deploy this verticle from this class
 * 
 */
public class Exercise2 {

    public static void main(String[] args) {
        // 1 - Create the Vert.x instance using Vertx.vertx (user io.vertx.core.Vertx)
        Vertx vertx = Vertx.vertx();

        // 2 - Deploy the Exercise2Verticle verticle using: vertx.deployVerticle(className)
        vertx.deployVerticle(Exercise2Verticle.class.getName());

    }
}