package com.eventdriven.eventbus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe in-memory event bus for asynchronous event publishing and handling.
 * Uses ExecutorService for async dispatch and ConcurrentHashMap for thread-safe handler registration.
 */
public class EventBus {
    
    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> handlers;
    private final ExecutorService executorService;
    
    public EventBus() {
        this.handlers = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    public EventBus(int threadPoolSize) {
        this.handlers = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    /**
     * Registers a handler for a specific event type.
     * Multiple handlers can be registered for the same event type.
     * 
     * @param eventType the class of the event to handle
     * @param handler the handler to invoke when events of this type are published
     */
    public <T extends Event> void registerHandler(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
    
    /**
     * Publishes an event to all registered handlers asynchronously.
     * Each handler is invoked in a separate thread from the executor pool.
     * 
     * @param event the event to publish
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        Class<?> eventType = event.getClass();
        List<EventHandler<? extends Event>> eventHandlers = handlers.get(eventType);
        
        if (eventHandlers != null) {
            for (EventHandler<? extends Event> handler : eventHandlers) {
                executorService.submit(() -> {
                    try {
                        ((EventHandler<T>) handler).handle(event);
                    } catch (Exception e) {
                        System.err.println("Error handling event: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        }
    }
    
    /**
     * Shuts down the event bus and waits for pending events to complete.
     * Should be called when the application is terminating.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
