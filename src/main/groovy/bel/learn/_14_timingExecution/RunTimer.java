package bel.learn._14_timingExecution;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.logging.Logger;

/**
 * See CollectionsLesson in _07_collections for example.
 * Very simple:
 *
 * RunTimer t = new RunTimer(); // starts it first time
 * doSomething();
 * t.stop(); // uses console to log the time
 *
 * t.go(); // starts again with set to 0
 * doSomethingElse();
 * t.stop("doSomethingElse"); // stops and provides timing and information.
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RunTimer {

    boolean running;
    long start;
    long end;
    long diff;
    Logger log;

    public RunTimer() {
        go();
    }

    public RunTimer(Logger log) {
        this.log = log;
        go();
    }

    public void go() {
        assert(!running);
        running=true;
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop(null);
    }

    public void stop(String explanation) {
        assert(running);
        end = System.currentTimeMillis();
        running=false;
        diff = end-start;
        String text="DURATION";
        if(explanation!=null && !"".equals(explanation)) {
            text+=" of "+explanation;
        }
        text+=":  "+ readableTime(diff);

        if(log == null) {
            System.out.println(text);
        } else {
            log.info(text);
        }

    }

    public static String readableTime(long millis) {
        long min =  millis / (1000 * 60);
        long sec =  (millis % (1000 *60)) / 1000;
        long ms = millis % 1000;
        return "" + min + " : " + sec + " : " + ms + " ( min : sec : ms )";

    }

}
