package io.vertx.workshop.exercise.microservice;

import io.vertx.reactivex.core.Vertx;

/**
 * A main class deploying the verticles
 * 
 * 
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.rxDeployVerticle(Exercise1Verticle.class.getName())
                .flatMap(x -> vertx.rxDeployVerticle(Exercise2Verticle.class.getName()))
                .flatMap(x -> vertx.rxDeployVerticle(Exercise3Verticle.class.getName()))
                .subscribe(
                    result ->  System.out.println(
                        "This is the result from deploying Ex. 2 & Ex. 3: " + result),
                    err -> err.printStackTrace()
                );
    }
    
}