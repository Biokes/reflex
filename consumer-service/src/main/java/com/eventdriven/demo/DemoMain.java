package com.eventdriven.demo;

import com.eventdriven.eventbus.EventBus;
import com.eventdriven.producer.PriceUpdatedEvent;

import java.util.Random;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DemoMain {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public static void main(String[] args) {
        EventBus bus = new EventBus();

        bus.registerHandler(PriceUpdatedEvent.class, event -> {
            String time = TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimestamp()));
            System.out.printf("[RECEIVED] %s | %s at $%.2f%n",
                    time, event.getSymbol(), event.getPrice());
        });

        new Thread(() -> {
            String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
            Random random = new Random();

            while (true) {
                try {
                    String symbol = symbols[random.nextInt(symbols.length)];
                    double price = 100 + (random.nextDouble() * 400);

                    PriceUpdatedEvent event = new PriceUpdatedEvent(symbol, price);
                    bus.publish(event);

                    System.out.printf("[PUBLISHED] %s at $%.2f%n", symbol, price);

                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();

        System.out.println("Demo running. Press Ctrl+C to stop.\n");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        bus.shutdown();
    }
}
