# Building Reactive Microservice Systems

This project is based mainly on the references below.

    <http://escoffier.me/vertx-kubernetes/>
    
    ESCOFFIER, C. Building Reactive Microservices in Java Asynchronous and Event-Based Application Design. First Edition. California: O’Reilly Media, Inc., 2017.

    RedHat Developer, accessed 1 November 2019, <https://developers.redhat.com/promotions/building-reactive-microservices-in-java>

    Kubernetes Hands-On - Deploy Microservices to the AWS Cloud 2018, Udemy, accessed 1 November 2019, <https://www.udemy.com/course/kubernetes-microservices>

    <https://github.com/hazelcast/hazelcast-code-samples/>

    <https://vertx.io/docs/vertx-hazelcast>


## Exercise 1 - Vert.x applications are Java applications

In this exercise, let's start from the very beginning.

    1. Create an instance of Vert.x.

    2. Start an Http server sending greetings.


    // 1 - Create the Vert.x instance using Vertx.vertx (use io.vertx.core.Vertx)
    Vertx vertx = Vertx.vertx();

    // 2 - Create a Http server using the `createHttpServer` method. Set a request handler doing:
        // `request.response().end("hello");
        // Call the listen method with '8080' as parameter.
    vertx.createHttpServer()
        .requestHandler(request -> request.response().end("hello"))
        .listen(8080);


## 3.5. Excercise 2 - Using Verticles

While using a main method is nice and simple, it does not necessarily scale.

When your code base grows, you need a better way to structure your code. For this, Vert.x provides verticles - a simple agent-like model. Verticles aree single-threaded classes interacting using asynchronous messages.

Run and check the result as in the previous exercise. Emit the request several times in a row to check that your verticle is always executed by the same thread.

            // Exercise2.java
            public static void main(String[] args) {
                // 1 - Create the Vert.x instance using Vertx.vertx (user io.vertx.core.Vertx)
                Vertx vertx = Vertx.vertx();

                // 2 - Deploy the Exercise2Verticle verticle using: vertx.deployVerticle(className)
                vertx.deployVerticle(Exercise2Verticle.class.getName());

            }


            // Exercise2Verticle.java
            @Override
            public void start() {
                // You can acces the Vertx.x instance you deployed on using the `vertx` (inherited) field

                // Create a Http server
                // Instead of "hello", display the name of the thread serving the request (using Thread.currentThread().getName())
                vertx.createHttpServer()
                    .requestHandler(request -> request.response().end(Thread.currentThread().getName()))
                    .listen(8080);
            }


## 3.6. Exercise 3 - Do not block the event loop

In this exercise, we are going to voluntarily break the golden rule - block the event loop.

In the Exercise2Verticle class, call sleep before writing the result into the response. 

When running this code and calling the server, you can see that the requests are not served in a timely fashion anymore. With the thread being blocked, it can't serve the subsequent requests before completing the first one.

Also notice the output in the console, Vert.x detects that the event loop has been blocked and starts yelling...

            vertx.createHttpServer()
                    .requestHandler(request -> {
                        sleep();
                        request.response().end(Thread.currentThread().getName());
                    })
                    .listen(8080);

You may wonder how you will be able to call blocking code. Don't worry, Vert.x provides several ways to do so. A construct named executeBlocking and a type of verticle named worker are not executed on the event loop.


## Exercise 4 - Sending and receiving messages

Verticles are a great way to structure your code, but how do verticles interact?

They use the event bus to send and receive messages.

Let's see how it works.

Exercise 4 is composed of 2 verticles: a sender and a receiver.

The sender emits a greeting message periodically. The receiver prints this message to the console. As Json is a very common format in Vert.x applications, this exercise also introduces the JsonObject, a facility to create and manipulate Json structures.


                // Sender
                @Override
                public void start() throws Exception {
                    
                    // Retrieve the event bus
                    EventBus eventBus = vertx.eventBus();

                    // Execute the give handler every 2000 ms
                    vertx.setPeriodic(2000, l -> {
                        // Use the eventBus() method to retrieve the event bus and send a "{'message': 'hello'}"
                        // JSON message on the "greetings" address


                        // 1 - Create the JSON object using the JsonObject class, and `put` the `message`:`hello`
                        // entry
                        JsonObject json = new JsonObject()
                            .put("message", "hello");


                        // 2 - Use the `send` method of the event bus to send the message. Messages sent with the
                        // `send` method are received by a single consumer. Messages sent with the `publish` method
                        // are received by all registered consumers.
                        eventBus.send("greetings", json);

                    }); 
                }



                // Receiver
                @Override
                public void start() throws Exception {
                    
                    // Retrieve the event bus and register a consumer on the "greetings" address. For each message,
                    // print it on the console. You can retrieve the message body using `body()`. Use the method 
                    // `encodePrettily` on the retrieved Json body to print it nicely.

                    vertx.eventBus().<JsonObject>consumer("greetings", msg -> {
                        System.out.println(msg.body().encodePrettily());
                    });

                }

             
## Exercise 5 - Request Reply and Composing actions

Let's now mix the Http server and the event bus. The first verticle creates an Http Server, but to respond to the request, it sends a message to another verticle and waits for a reply.

This reply is used as response to the Http request.

This introduces the request - reply delivery mechanism of the event bus.

This exercise is composed of a main class and and two verticles.

Let's start with the Receiver. Follow the instructions to receive messages from the greetings and reply to the received messages.


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


Then, edit the Http Sender file. In this verticle, we need to create an Http server. The requestHandler extracts the query parameter name, sends a message on the event bus, and writes the Http response when the reply from the event bus is received.

Launch the exercise using the Exercise5#main method. Check the result by opening your browser to http://localhost:8080 (should display hello world) and http://localhost:8080/?name=vert.x (should display hello vert.x)

This exercise shows how to compose asynchronous actions and how to use the AsyncResult structure. But as you can imagine, it quickly ends up with lots of callbacks. Let's move to the next example to show how RX Java can help in taming the asynchronous coordination.

## Exercise 6 - Use RX Java 2

This exercise is a rewrite of the previous one using RX Java 2. As mentioned above, RX Java is an implementation of the reactive programming principles     for Java. With this development model, we manipulate streams (called Flowable, Observable, Maybe, Single or Comparable depending on the number of items and their characteristics).

RX Java provides a lot of operators to compose streams together and so write asynchronous orchestration easily. This exercise is a very basic introduction to RX Java.

Open Exercise6HttpVerticle file and follow the instructions. Notice the import statements containing the reactivex package. This package contains the RX-ified Vert.x API.


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


## Demystifying microservices

Unless you spent the last year in a cave, you probably have heard about microservices. So what are microservices? To answer this question, let's quote from a veteran:

            "The microservice architectural style is an approach to developing a single application as a suite of small services, each running in its own process and communicating with lightweight mechanisms, often an Http resource API.

            These services are built around business capabilities and independently deployable by fully automated deployment machinery. There is a bare minimum of centralized management of these services, which may be written in different programming languages and use different data storage technologies."

                                                - Martin Fowler

## Microservices 

Why do we need microservices?

In one word: agility

Let's imagine, we have a rather large application. As a large application, the maintenance is a nightmare, adding features takes too much time, the technology used is very outdated (What? Corba is not cool anymore?), any change needs to pass a 50-steps process and be validated by 5 levels os management, the list goes on. Obviously, there are several teams on the application with different requirements and agendas. Well, we have such a monster app. How could we make the development and maintenance of this application efficient? Microservices are one answer to this question. It aims to reduce the time to production.

To do that end, the microservice architectural style proposes to:

    1. Split the application into a set of decoupled components providing defined services (defined means with a known interface or API)

    2. Allow the components to communicate with whatever protocol they choose, often REST, but not necessarily

    3. Allow the components to use whatever languages and technologies they want

    4. Allow each component to be developed, released, and deployed independently

    5. Allow the deployments to be automated in their own pipeline
    
    6. Allow the orchestration of the whole application to be reduced to the bare minimum

In this lab, we won't address point 5, but you should know that Vert.x does not restrict how you deploy your components. You can employ whatever technology best suites your environment. Whether it is ssh, ansible, puppet, docker, cloud, fabric8, or even floppy disk.

Point 6, however, is interesting and often misunderstood. It's pretty cool to develop independent pieces of software that magically interact at runtime. Yes, I said magically but in technology we don't believe in magic. To make this happen, what we need is some form of service discovery at runtime.

The service discovery mechanism can achieve its goal with any number of suitable means. These range from: hardcoding the service location in the code (which is generally a bad idea), using a DNS lookup service, or some more advanced techniques.

Having a service discovery mechanism allows our system components to interact transparently amongst each other regardless of location or environment. It also allows us to easily load-balance amongst our components through a round robin algorithm, for example, thereby making our system more fault-tolerant (by locating another service provider when one breaks down).

Although by definition, microservice applications are not required to be distributed, they usually are in practice. This comes with all the distributed application benefits and constraints: consensus computation (FLP), CAP theorem, consistency, monitoring, and many other reasons to fail. So microservice applications need to be designed to accomodate failures from their early implementation stage.

Before we go further, there are a couple of points I would like to mention. Microservices are not new and the concept is not rocket science. Academic papers from the 70's and 80's have defined (using different words) architectural styles very close to this. Another very important point to understand is: microservices are not a silver bullet. (Unless well managed) they have the capacity to increase the complexity of your application due to their distributed nature. Lastly, a microservice architecture will not fix all your issues.

The major concerns when it comes to microservices are rapid delivery, adaptation, independency and replaceability. Every microservice is made to be replaceable by another providing the same service / API / interface (at the core, it's basically an application of the Liskov substitution principle).

If you have been a developer for the last 10 years, you might want to to ask what's the difference between microservices and SOA. For a lot of people it's about size. This is not always true because services don't necessarily have to be small which makes the term microservice quite misleading. Microservices and SOA differ in purpose but the basic concepts are similar:

    * service: a defined feature accessible by an API, a client, a proxy, anything

    * service provider: a component implementing a service

    * service consumer: a component consuming a service

    * service discovery: the mechanism used by a consumer to find a provider

Both approaches inherit from Service Oriented Computing, aiming to decompose and manage independent pieces of software. You may have never heard about this even if you have used it: COM, Corba, Jini, OSGi, and web services are all different implementations of Service Oriented Computing.

Finally, there is a common misconception that microservices must be RESTful by nature. This couldn't be further from the truth. Microservices can employ any number interaction styles that best fit their purpose: RPC, events, messages, streams, etc. In this lab we will be using RESTful services, async RPC, and message sources.

It tends not to be easy to use service discovery when we are not used to it. So, let's do a few exercises about it. To continue introducing RX Java constructs, these exercises use the RX Java 2 API offered by Vert.x.


## Microservices - Exercise 1 - Publishing a Service

In this first exercise, we are just going to publish a service. A service is described by a record containing enough metadata for a consumer to select the right service and create a client.

Open the Exercise1Verticle.java file. This class already contains some code to crate an Http endpoint using Vert.x Web, exposed on the port 8080. Once ready to serve, and to let the other parts of your system consume this service, we need to advertise it. This is achieved in the publish method that you need to write.

First you need to create a record for the service. The io.vertx.reactivex.servicediscovery.types.HttpEndpoint class contains methods to create records for an Http endpoint. Once you have the record, you call rxPublish on the Vert.x service discovery.

This stores the record and let the other parts of your system be aware of its availability.

        private Single<Record> publish(io.vertx.core.http.HttpServer server) {

            // 1. Create a service record using `io.vertx.reactivex.servicediscovery.types.HttpEndpoint.
            // createRecord`. This record define the service name ("greetings"), the host ("localhost"),
            // the server port and the root ("/")
            Record record = HttpEndpoint.createRecord(
                "greetings", 
                    "localhost", 
                        server.actualPort(), 
                            "/");

            // 2 - Call rxPublish method with the created record and return the resulting single

            return discovery.rxPublish(record); 

        }

Notice also the stop method from the verticle class. When the verticle is stopped we must unregister the service. For this, the published rercord is stored and used in the rxUnpublish method. As this operation is also asynchronous, the stop method uses a Future to indicate the completion of the method.

        @Override
        public void stop(Future<Void> future) throws Exception {

            // Unregister the service when the verticle is stopped.
            // As it's an asynchronous operation, we use a `future` parameter to inidicate when the
            // operation has been completed
            discovery.rxUnpublish(record.getRegistration())
                    .subscribe(toObserver(future));

        }



## Microservices - Exercise 2 - Consuming a Service using Service References


The previous exercise has published a service. Vert.x provides two ways to retrieve this service: 

    * a low level API where the consumer retrieves the record (service description), retrieves a reference (service binding) and finally gets the client (service object)
    
    * a higher-level API managing the record and reference parts transparently.

    In this exercise, we use the first approach to better understand the different concepts. Open the Exercise2Verticle.java file and follow the instructions to retrieve the service. 

                map vs flatMap     - This code uses map and flapMap. map is used when the operation is synchronous and returns a non-reactive type. flatMap is used when the result is a reactive type (Single, Maybe, Completable, Observable or Flowable). The result denotes an asynchronous operation complete "later". When this operation progresses or completes,
                the next processing stage is executed.


                the WebClient is an asynchronous Vert.x Http client. To use it, create a `request using get (or one of the other proposed methods), indicating the path.
                Then call rxSend to create a Single (reactive type with a single result resolved "later"). Once you get the HttpResponse you can use the bodyAsString method to get the payload.

            private ServiceDiscovery discovery;

            @Override
            public void start() throws Exception {

                discovery = ServiceDiscovery.create(vertx);

                // 1 - Get the service record using `rxGetRecord`. Pass the lambda 
                // `svc -> svc.getName().equals("greetings")` as parameter to retrieve the service with the name
                // "greetings"
                // 2 - With the record (`.map`), get the service reference using `discovery.getReference`
                // 3 - With the reference (`.map`), get a WebClient (Vert.x http client) using 
                // `ref.getAs(WebClient.class)`
                // 4 - With the client (`.flatMapSingle`), invoke the service using: 
                // `client.get("/greetings/vert.x-low-level-api").rxSend()`
                // 5 - With the response (`.map`), extract the body as string (`bodyAsString` method)
                // 6 - Finally subscribe and print the result on the console

                discovery.rxGetRecord(svc -> svc.getName().equals("greetings"))
                
                    .map(record -> discovery.getReference(record))

                    .map(reference -> reference.getAs(WebClient.class))

                    .flatMapSingle(client -> client.get("/greetings/vert.x-low-level-api").rxSend())

                    .map(HttpResponse::bodyAsString)

                    .subscribe(

                        result -> System.out.println("Result from the greeting service (Consumer Ex. 2): " + result),

                        err -> { System.out.println("Error from Ex. 2 \n"); err.printStackTrace(); }

                    );
            }


## Exercise 3 - Consuming a service using sugars

This exercise is equivalent to the previous one but uses the higher-level API. Use the method io.vertx.reactivex.servicediscovery.types.HttpEndpoint#rxGetWebClient to retrieve WebClient directly.

                private ServiceDiscovery discovery;

                @Override
                public void start() throws Exception {
                    discovery = ServiceDiscovery.create(vertx);


                    // 1 - Get the Web Client using the `HttpEndpoint.rxGetWebClient` method. Use the same lambda as in the
                    // previous exercise.
                    // 2 - Invoke the HTTP service as in the previous exercise
                    // 3 - Extract the body as String
                    // 4 - Subscribe and display the result on the console
                    HttpEndpoint.rxGetWebClient(discovery, svc -> svc.getName().equals("greetings"))
                        .flatMap(client -> client.get("/greetings/vert.x").rxSend())
                        .map(HttpResponse::bodyAsString)
                        .subscribe(
                            result -> System.out.println("Result from the greeting service (Consumer Ex. 3)" + result),
                            err -> { System.out.println("Error from Ex. 3 \n"); err.printStackTrace(); }
                        );

                    
                }

