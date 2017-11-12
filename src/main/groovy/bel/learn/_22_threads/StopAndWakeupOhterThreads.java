package bel.learn._22_threads;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility to preemtively stop other threads and wake them up again...
 * If stopAllOthers() is called, then tryWait() and shouldStop() return true.
 * If waitAllOthers() is called, then tryWait() waits until notifyAllOthers() is called or interrupt is called.
 * if stopAllOthers() is called, then tryWait() and shouldStop() return true.
 *
 */
@Log4j2
public class StopAndWakeupOhterThreads {

    AtomicBoolean waitAllOthers = new AtomicBoolean(false);
    AtomicBoolean stopAllOthers = new AtomicBoolean(false);

    /**
     * from now on, all other threads will wait, if they call "tryWait()"
     */
    public void waitAllOthers() {
        waitAllOthers.set(true);
    }

    /**
     * all other threads get signaled, that they should return from run - meaning stop.
     */
    public void stopAllOthers() {
        stopAllOthers.set(true);
    }

    /**
     * call notify to all waiting threads. They will return from tryWait.
     */
    public void notifyAllOthers() {
        waitAllOthers.set(false);
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * a thread can make all other threads wait.
     * The "other threads" call permanently tryWait().
     * The "one thread" calls waitAllOthers().
     * To wake up all the others, the one calls notifyAllOthers():
     * @return if the calling thread should be stopped.
     * Stopping could be done by
     * - a call to interrupt,
     * - calling "stopAllOthers"
     * -
     */
    public boolean tryWait() {
            try {
                if(stopAllOthers.get()) {return true;}
                if(waitAllOthers.get()) {
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                log.catching(e);
                return true;
            }
            return stopAllOthers.get();
    }

    /**
     * if stopAllOthers() is called, then tryWait() and shouldStop() return true.
     * @return true, if stopAllOthers() was called. Signals the tread to stop.
     */
    public boolean shouldStop() {
        return stopAllOthers.get();
    }

}
