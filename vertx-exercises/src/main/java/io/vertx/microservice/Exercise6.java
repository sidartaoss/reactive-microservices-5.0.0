package io.vertx.microservice;

import io.vertx.core.Vertx;

/**
 * 
 * Launcher for the Exercise 6.
 * 
 * 
 */
public class Exercise6 {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Exercise6HttpVerticle.class.getName());
        vertx.deployVerticle(Exercise5ProcessorVerticle.class.getName());
    }
    
}