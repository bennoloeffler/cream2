package bel.learn._00_logging.package1;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.ObjectMessage;

/**
 * Demonstrate tracing
 */
@Log4j2
public class TraceThisClass {

    public int  tracedMethod(int x) {
        log.traceEntry(new ObjectMessage(x));
        if(x>1000) throw new RuntimeException("too big"); // this is traced outside...
        int result = x*x;
        return log.traceExit(result);
    }
}
