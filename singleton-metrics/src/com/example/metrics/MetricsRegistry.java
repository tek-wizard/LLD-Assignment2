package com.example.metrics;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * INTENTION: Global metrics registry (should be a Singleton).
 *
 * CURRENT STATE (BROKEN ON PURPOSE):
 * - Constructor is public -> anyone can create instances.
 * - getInstance() is lazy but NOT thread-safe -> can create multiple instances.
 * - Reflection can call the constructor to create more instances.
 * - Serialization can create a new instance when deserialized.
 *
 * TODO (student):
 *  1) Make it a proper lazy, thread-safe singleton (private ctor)
 *  2) Block reflection-based multiple construction
 *  3) Preserve singleton on serialization (readResolve)
 */
public class MetricsRegistry implements Serializable {
    private static final long serialVersionUID = 1L;

    // Volatile is essential for Double-Checked Locking visibility
    private static volatile MetricsRegistry instance;
    
    // Using ConcurrentHashMap for thread-safe storage
    private final Map<String, Long> counters = new ConcurrentHashMap<>();

    // 1) Private constructor prevents direct instantiation
    private MetricsRegistry() {
        // 2) Reflection Shield: Blocks multiple constructions
        if (instance != null) {
            throw new IllegalStateException("Instance already exists. Use getInstance().");
        }
    }

    // 1) Lazy, thread-safe initialization via Double-Checked Locking
    public static MetricsRegistry getInstance() {
        if (instance == null) {
            synchronized (MetricsRegistry.class) {
                if (instance == null) {
                    instance = new MetricsRegistry();
                }
            }
        }
        return instance;
    }

    // 3) readResolve preserves singleton identity during deserialization
    protected Object readResolve() {
        return getInstance();
    }

    public void setCount(String key, long value) {
        counters.put(key, value);
    }

    public void increment(String key) {
        // Atomic update using ConcurrentHashMap logic
        counters.merge(key, 1L, Long::sum);
    }

    public long getCount(String key) {
        return counters.getOrDefault(key, 0L);
    }

    public Map<String, Long> getAll() {
        return Collections.unmodifiableMap(new HashMap<>(counters));
    }
}
