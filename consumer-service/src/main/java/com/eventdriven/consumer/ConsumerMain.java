package com.eventdriven.consumer;

import com.eventdriven.eventbus.EventBus;
import com.eventdriven.producer.PriceUpdatedEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Consumer service that listens for and processes PriceUpdatedEvent.
 * Prints received events to console with formatted output.
 */
public class ConsumerMain {
    
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    
    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        
        eventBus.registerHandler(PriceUpdatedEvent.class, event -> {
            String time = TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimestamp()));
            System.out.printf("[RECEIVED] %s | %s at $%.2f%n", 
                            time, event.getSymbol(), event.getPrice());
        });
        
        System.out.println("Consumer Service Started");
        System.out.println("Listening for price update events...");
        System.out.println("Press Ctrl+C to stop\n");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down consumer...");
            eventBus.shutdown();
        }));
        
        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        eventBus.shutdown();
    }
}
