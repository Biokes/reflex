package com.eventdriven.producer;

import com.eventdriven.eventbus.EventBus;

import java.util.Random;

/**
 * Producer service that publishes PriceUpdatedEvent every 2 seconds.
 * Simulates a price feed for stock symbols.
 */
public class ProducerMain {
    
    private static final String[] SYMBOLS = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        
        System.out.println("Producer Service Started");
        System.out.println("Publishing price updates every 2 seconds...");
        System.out.println("Press Ctrl+C to stop\n");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down producer...");
            eventBus.shutdown();
        }));
        
        while (true) {
            try {
                String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                double price = 100 + (random.nextDouble() * 400);
                
                PriceUpdatedEvent event = new PriceUpdatedEvent(symbol, price);
                eventBus.publish(event);
                
                System.out.printf("[PUBLISHED] %s at $%.2f%n", symbol, price);
                
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        eventBus.shutdown();
    }
}
