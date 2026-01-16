# Event-Driven System (In-Memory, Single-JVM)

A minimal, fully understandable event-driven system using a Maven multi-module layout. It focuses on clarity over features and demonstrates an in-memory, thread-safe Event Bus with asynchronous dispatch.

Important: This Event Bus is in-memory and process-local. Two separate JVM processes do not share an event bus. The provided producer-service and consumer-service are standalone demos; they will not communicate with each other when run as separate processes.

## Project Structure

```
reflex/
├── pom.xml                    # Parent aggregator POM
├── event-bus/                 # Shared event bus library
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/eventdriven/eventbus/
│       │   ├── Event.java
│       │   ├── EventHandler.java
│       │   └── EventBus.java
└── consumer-service/          # Demo: registers a handler for PriceUpdatedEvent
    ├── pom.xml
    └── src/main/java/
        ├── com/eventdriven/consumer/
        │   └── ConsumerMain.java
        └── com/eventdriven/demo/
            └── DemoMain.java   # Single-JVM end-to-end demo using a shared EventBus
├── producer-service/          # Demo: publishes PriceUpdatedEvent periodically
│   ├── pom.xml
│   └── src/main/java/com/eventdriven/producer/
│       ├── PriceUpdatedEvent.java
│       └── ProducerMain.java
└── consumer-service/          # Demo: registers a handler for PriceUpdatedEvent
    ├── pom.xml
    └── src/main/java/com/eventdriven/consumer/
        └── ConsumerMain.java
```

## How It Works

- event-bus: Pure Java class providing thread-safe, asynchronous event publishing using ExecutorService.
- Event: Marker interface with a timestamp for immutable domain events.
- EventHandler<T>: Functional interface for type-safe event handling.
- EventBus: Maintains handlers in ConcurrentHashMap with CopyOnWriteArrayList, publishes events asynchronously via a fixed thread pool, and supports graceful shutdown.

Asynchronous communication demo: DemoMain wires a producer and consumer to the same EventBus instance within one JVM. Published events appear immediately and are handled asynchronously by the registered consumer handler.

## Requirements

- Java 17 or higher
- Maven 3.6+

## Build

From the repository root:

```
mvn clean compile
```

Notes:
- clean compile is sufficient for running the in-repo demo classes from target/classes.
- There are currently no test classes in this repository.
- If you want to build JARs, see the JAR section below.

## Running the Demos

Because the Event Bus is in-memory and not distributed, each JVM has its own bus instance. Therefore, running ProducerMain and ConsumerMain in separate terminals will not result in messages being delivered between them. Use the following demos as separate examples.

### Single-JVM End-to-End Demo (Recommended)

A self-contained demo is included at consumer-service/src/main/java/com/eventdriven/demo/DemoMain.java. It creates a single EventBus, registers a consumer, and publishes events periodically in the same JVM, demonstrating asynchronous handling end-to-end.

Run from the repository root (Windows classpath separator ; and backslashes for paths):

```
mvn clean compile
java -cp "event-bus\\target\\classes;producer-service\\target\\classes;consumer-service\\target\\classes" com.eventdriven.demo.DemoMain
```

On Linux/macOS use : as the classpath separator and forward slashes for paths:

```
mvn clean compile
java -cp "event-bus/target/classes:producer-service/target/classes:consumer-service/target/classes" com.eventdriven.demo.DemoMain
```

Expected output (example):
```
Demo running. Press Ctrl+C to stop.
[PUBLISHED] AAPL at $486.14
[RECEIVED] 23:07:51 | AAPL at $486.14
[PUBLISHED] AMZN at $487.14
[RECEIVED] 23:07:54 | AMZN at $487.14
[PUBLISHED] TSLA at $307.34
[RECEIVED] 23:07:56 | TSLA at $307.34
[PUBLISHED] AAPL at $243.26
[RECEIVED] 23:07:58 | AAPL at $243.26
```

### Separate Producer and Consumer Demos

- Producer demo (publishes events and logs publish messages):
  ```
  cd producer-service
  mvn clean compile
  java -cp "..\\event-bus\\target\\classes;target\\classes" com.eventdriven.producer.ProducerMain
  ```

- Consumer demo (registers a handler and waits; only receives events published on the same EventBus instance within the same JVM):
  ```
  cd consumer-service
  mvn clean compile
  java -cp "..\\event-bus\\target\\classes;..\\producer-service\\target\\classes;target\\classes" com.eventdriven.consumer.ConsumerMain
  ```

These processes are independent and will not communicate with each other.

### Why two processes won't talk

- The Event Bus is an in-memory data structure. It is not a networked or distributed message broker.
- Separate Java processes (JVMs) have separate memory spaces and therefore separate EventBus instances.

### End-to-End (single JVM) options

If you want to see end-to-end publishing and consuming together, use one of the following approaches:

- Use the unit tests in event-bus (see Running Tests) which demonstrate handlers receiving events.
- Create a simple single-JVM launcher that:
  - Instantiates one EventBus
  - Registers the consumer handler (ConsumerMain logic)
  - Publishes price events periodically (ProducerMain logic)

This repository purposefully keeps producer and consumer as separate, minimal demos to focus on the Event Bus API. A combined demo can be added as an exercise if desired.

## Running JARs

You can package the modules as JARs if you prefer to run from artifacts:

```
mvn clean package
```

Windows examples:
- Producer from JAR:
  ```
  java -cp "producer-service\\target\\producer-service-1.0.0.jar;event-bus\\target\\event-bus-1.0.0.jar" com.eventdriven.producer.ProducerMain
  ```
- Consumer from JAR:
  ```
  java -cp "consumer-service\\target\\consumer-service-1.0.0.jar;event-bus\\target\\event-bus-1.0.0.jar;producer-service\\target\\producer-service-1.0.0.jar" com.eventdriven.consumer.ConsumerMain
  ```

On Linux/macOS replace ; with : and \\ with /.

Again, these are independent demos and will not communicate across processes.

## Running Tests

There are currently no automated tests in this repository.

## API Overview

- Event
  - Marker interface for immutable events with a `long getTimestamp()`.
- EventHandler<T extends Event>
  - Functional interface: `void handle(T event)`.
- EventBus
  - `registerHandler(Class<T> eventType, EventHandler<T> handler)`
  - `publish(T event)` (async dispatch to all handlers registered for event type)
  - `shutdown()` (graceful executor shutdown)

## Extending the System

- Add new events: Create a class implementing `Event` and make it immutable.
- Add handlers: Implement `EventHandler<MyEvent>` and register it via `eventBus.registerHandler(MyEvent.class, handler)`.
- Control concurrency: Use `new EventBus(threadPoolSize)` to tune the executor size.
- Error handling: Handlers are wrapped with try/catch during dispatch; extend or adapt as needed.

## Design Choices

1. Thread Safety: ConcurrentHashMap and CopyOnWriteArrayList for handler storage.
2. Async Execution: Fixed thread pool via ExecutorService for non-blocking dispatch.
3. Type Safety: Generics on EventHandler<T> ensure compile-time type checks.
4. Simplicity: No frameworks, annotations, or external runtime dependencies.
5. Graceful Shutdown: Shutdown hooks recommended in demos.

## Limitations (By Design)

- In-memory only (events not persisted)
- Single JVM (no distributed messaging between processes)
- No event replay or durable history
- No guaranteed delivery or transactions

These constraints are intentional to keep the project minimal.
