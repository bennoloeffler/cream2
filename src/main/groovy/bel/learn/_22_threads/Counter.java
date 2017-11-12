package bel.learn._22_threads;

import lombok.Getter;

/**
 * helps demonstrating race condition
 */
public class Counter {

    @Getter
    int count = 0;

    void increment() {
        count = count + 1;
    }

    synchronized void incrementSynced() {
        count = count + 1;
    }
}
