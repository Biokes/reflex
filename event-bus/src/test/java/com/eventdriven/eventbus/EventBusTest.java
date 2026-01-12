package com.eventdriven.eventbus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {
    
    private EventBus eventBus;
    
    @BeforeEach
    void setUp() {
        eventBus = new EventBus(2);
    }
    
    @AfterEach
    void tearDown() {
        eventBus.shutdown();
    }
    
    @Test
    void testHandlerReceivesPublishedEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TestEvent[] receivedEvent = new TestEvent[1];
        
        eventBus.registerHandler(TestEvent.class, event -> {
            receivedEvent[0] = event;
            latch.countDown();
        });
        
        TestEvent testEvent = new TestEvent("test-data");
        eventBus.publish(testEvent);
        
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Handler should be called within timeout");
        assertNotNull(receivedEvent[0]);
        assertEquals("test-data", receivedEvent[0].getData());
    }
    
    @Test
    void testMultipleHandlersReceiveSameEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger callCount = new AtomicInteger(0);
        
        EventHandler<TestEvent> handler = event -> {
            callCount.incrementAndGet();
            latch.countDown();
        };
        
        eventBus.registerHandler(TestEvent.class, handler);
        eventBus.registerHandler(TestEvent.class, handler);
        eventBus.registerHandler(TestEvent.class, handler);
        
        eventBus.publish(new TestEvent("multi-handler-test"));
        
        assertTrue(latch.await(2, TimeUnit.SECONDS), "All handlers should be called");
        assertEquals(3, callCount.get());
    }
    
    @Test
    void testHandlerNotCalledForDifferentEventType() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);
        
        eventBus.registerHandler(TestEvent.class, event -> {
            callCount.incrementAndGet();
            latch.countDown();
        });
        
        eventBus.publish(new AnotherTestEvent("different-type"));
        
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS), "Handler should not be called for different event type");
        assertEquals(0, callCount.get());
    }
    
    // Test event classes
    static class TestEvent implements Event {
        private final String data;
        private final long timestamp;
        
        TestEvent(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        String getData() {
            return data;
        }
        
        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    static class AnotherTestEvent implements Event {
        private final String data;
        private final long timestamp;
        
        AnotherTestEvent(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
}
