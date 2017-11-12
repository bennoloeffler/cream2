package bel.learn._22_threads;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import static bel.learn._22_threads.ConcurrentUtils.sleep;
import static bel.learn._22_threads.ConcurrentUtils.stop;
import static bel.util.Util.readableTime;
import static java.lang.Thread.currentThread;

/**
 * shows thread examples.
 * @link http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
 */
public class MainThreadExamples {
    public static void main(String[] args) {
        MainThreadExamples ex = new MainThreadExamples();
        try {

            ex.raceCondition();
            ex.noRaceCondition();
            ex.threadsStart();
            ex.waitNotify();
            ex.executor();
            ex.callable();
            ex.poolOfCallables();
            ex.scheduledExecution();
            ex.readWriteLock();
            ex.atomicTypes();
            ex.concurrentMap();
            ex.queue();
            ex.syncingThreads();
            ex.stoppingThreads();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void queue() throws InterruptedException {
        pr("");
        pr("");
        pr("---- QUEUE ----");
        Random r = new Random();

        String[] strings = {"111111111111111","2", "3", "44444444444444444444444", "5555555", "6", "77", "88", "999999", "END"};
        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>(2);


        Runnable putter = () -> {
            for(String s : strings) {
                try {
                    sleep(r.nextInt(500));
                    pr("sending: " + s);
                    q.put(s);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            pr("finished putter");
        };


        Runnable taker = () -> {
            String s;
            do {
                try {
                    sleep(r.nextInt(1000));
                    s = q.take();
                    pr("getting: " + s);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(s != "END");
            pr("finished getter");

        };

        Thread t1 = new Thread(putter);
        t1.start();
        Thread t2 = new Thread(taker);
        t2.start();

    }

    private void stoppingThreads() {
        pr("");
        pr("");
        pr("---- STOPPING THREADS ----");
        StopAndWakeupOhterThreads stopAndWakeupOhterThreads = new StopAndWakeupOhterThreads();

        Runnable r = () -> {
            try {
                int i = 0;
                do {
                    boolean stop = stopAndWakeupOhterThreads.tryWait();
                    if (stop) {
                        System.out.println("Stopping thread: " +  currentThread().getName());
                        return;
                    }
                    sleep(100);
                    System.out.println(" " + i++ + ":" + currentThread().getName());
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        System.out.println("");
        System.out.println("started all threads. Running for 1 seconds");
        System.out.println("");

        ArrayList<Thread> threads = new ArrayList<>();
        for(int i = 0; i<3; i++) {
            Thread t = new Thread(r);
            t.start();
            threads.add(t);
        }
        sleep(1000);

        stopAndWakeupOhterThreads.waitAllOthers();
        System.out.println("");
        System.out.println("stopped all. For 1 seconds...");
        sleep(1000);

        System.out.println("");
        System.out.println("re-starting all threads. Running for 1 seconds");
        stopAndWakeupOhterThreads.notifyAllOthers();
        sleep(1000);


        System.out.println("now stopping all");
        stopAndWakeupOhterThreads.stopAllOthers();

        /* therefore, interupted is not needed.
        for (Thread thread : threads) {
            thread.interrupt();
        }*/

    }

    private void syncingThreads() {
        WaitForNotification waiter = new WaitForNotification();
        Thread t1 = new Thread(()-> {
            pr("going to wait..." + currentThread().getName());
            waiter.stopAndWaitForNotification();
            pr("woke up..." + currentThread().getName());
        });
        t1.start();
        sleep(1000);
        Thread t2 = new Thread(()-> {
            pr("going to wake up the other tread from this thread..." + currentThread().getName());
            waiter.notifyToStartAgain();
            pr("also running further..." + currentThread().getName());
        });
        t2.start();

    }

    private void concurrentMap() {
        pr("");
        pr("");
        pr("---- CONCURRENT MAP ----");
        pr("cores or processors for parallel execution of map functions: "+ForkJoinPool.getCommonPoolParallelism());  // how much?
        // This value can be decreased or increased by setting the following JVM parameter:
        // -Djava.util.concurrent.ForkJoinPool.common.parallelism=5
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("foo", "bar");
        map.put("han", "solo");
        map.put("r2", "d2");
        map.put("c3", "p0");

        //map.forEach((key, value) -> System.out.printf("%s = %s\n", key, value));

        pr("");
        pr("now using concurrent hash map implementation...");
        map.forEach(1, (key, value) -> {
            System.out.printf("key: %s; value: %s; thread: %s\n", key, value, currentThread().getName());
        });
        pr("finished map");

    }

    private void atomicTypes() {
        pr("");
        pr("");
        pr("---- ATOMIC TYPE ----");
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(5);

        IntStream.range(0, 1000)
                .forEach(i -> executor.submit(atomicInt::incrementAndGet));
        sleep(100);
        pr("1000 x increment: " + atomicInt.get());

        AtomicLong atomicLong = new AtomicLong(1);
        IntStream.range(1, 15)
                .forEach(i -> {
                    Runnable task = () ->
                            atomicLong.accumulateAndGet(i, (n, m) -> n*m);
                    executor.submit(task);
                });
        sleep(1500);
        pr("n*m: " + atomicLong.get());
        sleep(500);
        stop(executor);

    }

    private void noRaceCondition() {
        pr("");
        pr("");
        pr("---- NO RACE CONDITION ----");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Counter c = new Counter();
        IntStream.range(0, 100000).forEach(i -> executor.submit(c::incrementSynced));
        stop(executor);
        pr(Integer.toString(c.getCount()));  // e.g. 100000, because of reading and adding is synced... no writes are "lost"
    }

    private void readWriteLock() {
        pr("");
        pr("");
        pr("---- READ AND WRITES LOCKS ----");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        executor.submit(() -> {
            lock.writeLock().lock();
            try {
                sleep(1000);
                pr("put to map");
                map.put("foo", "bar");
            } finally {
                lock.writeLock().unlock();
            }
        });

        Runnable readTask = () -> {
            lock.readLock().lock();
            try {
                pr(map.get("foo"));
                sleep(1000);
            } finally {
                lock.readLock().unlock();
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        stop(executor);
    }

    private void raceCondition() {
        pr("");
        pr("");
        pr("---- RACE CONDITION ----");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Counter c = new Counter();
        IntStream.range(0, 100000).forEach(i -> executor.submit(c::increment));
        stop(executor);
        pr(Integer.toString(c.getCount()));  // e.g. 99994, because of reading deliveres same value... writes are "lost"
    }

    private void scheduledExecution() throws InterruptedException {
        pr("");
        pr("");
        pr("---- SCHEDULED EXECUTION ----");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = () -> System.out.println("Excecution at time: " + readableTime(System.currentTimeMillis()));
        int initialDelay = 100;
        int period = 300;
        executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
        sleep(3000);
        executor.shutdown();
    }

    private void poolOfCallables() throws ExecutionException, InterruptedException {
        pr("");
        pr("");
        pr("---- CALLABLE POOL ----");
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<String>> callables = Arrays.asList(
                createCallable("task1", 2),
                createCallable("task2", 1),
                createCallable("task3", 3)
                //createCallable("task4", 4),
                //createCallable("task5", 5),
                //createCallable("task6", 6),
                //createCallable("task7", 7)
                );
        String result = executor.invokeAny(callables); // fastest
        pr(result);
        pr("Done invokeAny(...)");
        pr("");

        List<Future<String>> futures = executor.invokeAll(callables); // all finished
        //futures.forEach((dataFile)->pr("result: " + dataFile.get())); //Exception... does not work, because of declaration
        for(Future f: futures) {
            pr("result: " + f.get());
        }
        pr("Done invokeAll(...)");
        pr("");

        List<Future<String>> otherFutures = new ArrayList<>();
        callables.forEach((task) -> {
            Future<String> f = executor.submit(task);
            otherFutures.add(f);
        });
        List<Future<String>> count = new ArrayList<>(otherFutures);
        do {
            for(Future<String> f: otherFutures) {
                if(f.isDone() && count.contains(f)) {
                    String r = f.get();
                    pr("result: " + r);
                    count.remove(f);
                }
            }
            System.out.print(".");
            sleep(100);

        }while (count.size() > 0);
        pr("Done individual submit(...)");
        pr("");

        executor.shutdown();
    }

    private void callable() throws ExecutionException, InterruptedException {
        pr("");
        pr("");
        pr("---- CALLABLE and FUTURES ----");
        Callable<Integer> task = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                return 123;
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(task);

        System.out.println("future done? " + future.isDone());

        Integer result = future.get();

        System.out.println("future done? " + future.isDone());
        System.out.print("result: " + result);
        executor.shutdown();
    }

    private void executor() throws InterruptedException {
        pr("");
        pr("");
        pr("---- EXECUTOR ----");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable r = () -> {
            String threadName = currentThread().getName();
            pr("Hello " + threadName);
            int i = 0;
            do {
                System.out.print("  num: " + ++i );
            } while (i<10000);
            pr("");
            pr("numbers finished");
        };
        executor.submit(r);
        try {
            pr("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            pr("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                pr("cancel non-finished tasks");
            }
            executor.shutdownNow();
            pr("shutdown finished");
        }
    }

    private void threadsStart() throws InterruptedException {
        pr("");
        pr("");
        pr("---- THREAD Names ----");

        Runnable task = () -> {
            String threadName = currentThread().getName();
            pr("Hello " + threadName);
        };

        task.run();

        Thread thread = new Thread(task);
        thread.start();
        sleep(200);

        pr("Done!");
    }

    private void waitNotify() throws Exception {
        pr("");
        pr("");
        pr("---- WAIT NOTIFY ----");
        Waiting waiting = new Waiting();
        Thread thread = new Thread(() -> {
            waiting.doWait();
        });
        thread.start();


        TimeUnit.SECONDS.sleep(1);
        pr("thread Waiting still not awake...");
        sleep(300);
        pr("thread Waiting still not awake...");
        sleep(300);
        pr("thread Waiting still not awake...");

        synchronized (waiting) {
            pr("thread Waiting is now notified...");
            waiting.notify();
        }
        sleep(300);
        pr("thread Waiting should have finished now...");
    }

    private void pr(String s) {
        System.out.println(s);
    }

    Callable<String> createCallable(String result, long sleepSeconds) {
        return () -> {
            TimeUnit.SECONDS.sleep(sleepSeconds);
            return result;
        };
    }
}
