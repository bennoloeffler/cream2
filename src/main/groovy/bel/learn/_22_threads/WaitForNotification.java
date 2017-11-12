package bel.learn._22_threads;

import lombok.extern.log4j.Log4j2;

/**
 * This can be used to stop one thread and wait for another
 */
@Log4j2
public class WaitForNotification {

    public void stopAndWaitForNotification() {
         synchronized (this) {
             try {
                 wait();
             } catch (InterruptedException e) {
                log.catching(e);
                 throw new RuntimeException(e);
             }
         }
    }

    public void notifyToStartAgain() {
        synchronized (this) {
            notify();
        }
    }


}
