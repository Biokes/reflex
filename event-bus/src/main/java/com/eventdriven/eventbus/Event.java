package com.eventdriven.eventbus;

/**
 * Marker interface for all events in the system.
 * Events are immutable data objects that represent something that happened.
 */
public interface Event {
    /**
     * Returns the timestamp when this event was created.
     */
    long getTimestamp();
}
