package bel.learn._22_threads;

/**
 * This class waits...
 */
public class Waiting  {
    public void doWait()  {
        try {
            System.out.println("before waiting. now going to wait for notify...");
            synchronized (this) {
                wait();
            }
            System.out.println("after waiting. finished!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
