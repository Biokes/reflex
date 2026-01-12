# Event-Driven System

A minimal, fully understandable event-driven system using Maven monorepo architecture.

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
│       └── test/java/com/eventdriven/eventbus/
│           └── EventBusTest.java
├── producer-service/          # Event publisher
│   ├── pom.xml
│   └── src/main/java/com/eventdriven/producer/
│       ├── PriceUpdatedEvent.java
│       └── ProducerMain.java
└── consumer-service/          # Event consumer
    ├── pom.xml
    └── src/main/java/com/eventdriven/consumer/
        └── ConsumerMain.java
```

## Architecture

- **event-bus**: Pure Java library providing thread-safe, asynchronous event publishing using `ExecutorService`
- **producer-service**: Publishes `PriceUpdatedEvent` every 2 seconds with random stock prices
- **consumer-service**: Listens for and prints received price events

## Requirements

- Java 17 or higher
- Maven 3.6+

## Building the Project

From the root directory:

```bash
mvn clean install
```

This will:
1. Build the event-bus library
2. Run unit tests
3. Build producer-service
4. Build consumer-service

## Running the System

### Option 1: Run with Maven (Recommended for Testing)

**Terminal 1 - Start Consumer:**
```bash
cd consumer-service
mvn exec:java -Dexec.mainClass="com.eventdriven.consumer.ConsumerMain"
```

**Terminal 2 - Start Producer:**
```bash
cd producer-service
mvn exec:java -Dexec.mainClass="com.eventdriven.producer.ProducerMain"
```

### Option 2: Run JAR Files

**Build JARs with dependencies:**
```bash
mvn clean package
```

**Terminal 1 - Start Consumer:**
```bash
java -cp "consumer-service/target/consumer-service-1.0.0.jar;event-bus/target/event-bus-1.0.0.jar;producer-service/target/producer-service-1.0.0.jar" com.eventdriven.consumer.ConsumerMain
```

**Terminal 2 - Start Producer:**
```bash
java -cp "producer-service/target/producer-service-1.0.0.jar;event-bus/target/event-bus-1.0.0.jar" com.eventdriven.producer.ProducerMain
```

## Running Tests

```bash
cd event-bus
mvn test
```

## Expected Behavior

**Producer Output:**
```
Producer Service Started
Publishing price updates every 2 seconds...
Press Ctrl+C to stop

[PUBLISHED] AAPL at $245.67
[PUBLISHED] GOOGL at $312.89
[PUBLISHED] TSLA at $189.34
```

**Consumer Output:**
```
Consumer Service Started
Listening for price update events...
Press Ctrl+C to stop

[RECEIVED] 15:19:45 | AAPL at $245.67
[RECEIVED] 15:19:47 | GOOGL at $312.89
[RECEIVED] 15:19:49 | TSLA at $189.34
```

## Key Design Decisions

1. **Thread Safety**: Uses `ConcurrentHashMap` and `CopyOnWriteArrayList` for handler storage
2. **Async Execution**: `ExecutorService` with fixed thread pool for non-blocking event dispatch
3. **Type Safety**: Generic `EventHandler<T>` ensures compile-time type checking
4. **Simplicity**: No frameworks, no annotations, no external dependencies (except JUnit for tests)
5. **Graceful Shutdown**: Shutdown hooks ensure proper cleanup of thread pools

## Limitations (By Design)

- In-memory only (events not persisted)
- Single JVM (no distributed messaging)
- No event replay or history
- No guaranteed delivery
- No transaction support

This is intentional - the system is designed as a learning foundation for understanding event-driven patterns.
