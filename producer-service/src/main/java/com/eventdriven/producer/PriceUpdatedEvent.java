package com.eventdriven.producer;

import com.eventdriven.eventbus.Event;

/**
 * Event representing a price update for a trading symbol.
 * Immutable event object containing symbol and new price.
 */
public class PriceUpdatedEvent implements Event {
    
    private final String symbol;
    private final double price;
    private final long timestamp;
    
    public PriceUpdatedEvent(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public double getPrice() {
        return price;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("PriceUpdatedEvent{symbol='%s', price=%.2f, timestamp=%d}", 
                           symbol, price, timestamp);
    }
}
