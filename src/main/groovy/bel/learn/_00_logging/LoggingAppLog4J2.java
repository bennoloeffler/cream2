package bel.learn._00_logging;

import bel.learn._00_logging.package1.TraceThisClass;
import bel.learn._00_logging.package2.AnotherClassWithMarkers;
import bel.learn._00_logging.package2.ClassWithMarkers;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;


/**
 * @link http://logging.apache.org/log4j/2.x/manual/api.html
 * @link http://tutorials.jenkov.com/java-logging/configuration.html
 * @link http://www.codejava.net/coding/common-conversion-patterns-for-log4js-patternlayout
 * @see <a href="http://www.torsten-horn.de/techdocs/java-log4j.htm#Log4j2">http://www.torsten-horn.de/techdocs/java-log4j.htm#Log4j2</a><br/>
 *
 * TO REMEMBER filters are "added": 1. Loggers level (context hierarchy)  2. filter im logger, eg Marker  3. Filter im Appender, e.g. with filter
 *
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
<Appenders>
<Console name="Console" target="SYSTEM_OUT">
<!--<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/> -->
<PatternLayout pattern="%-5level %msg - %logger{36}.%M (%F:%L)%n"/>
</Console>
<File name="File" fileName="testLog4jFile.txt">
<MarkerFilter marker="CALC_MARKER" onMatch="ACCEPT" onMismatch="DENY"/>
<PatternLayout>
<pattern>%d %p %m (%F:%L)%n</pattern>
</PatternLayout>
</File>
</Appenders>
<Loggers>

<Logger name="bel.learn._00_logging.package2" level="debug" additivity="false">
<MarkerFilter marker="CALC_MARKER" onMatch="ACCEPT" onMismatch="DENY"/>
<AppenderRef ref="File"/>
<AppenderRef ref="Console"/>
</Logger>


<Logger name="bel.learn._00_logging.package1" level="trace" additivity="false">
<AppenderRef ref="Console"/>
</Logger>

<Root level="error">
<AppenderRef ref="Console"/>
</Root>

</Loggers>
</Configuration>
 */
@Log4j2
public class LoggingAppLog4J2 {
    /*
 * Log levels have to be set in log4j2.xml
 * OFF	0
 * FATAL	100
 * ERROR	200
 * WARN	    300
 * INFO	    400
 * DEBUG	500
 * TRACE	600
 * ALL	    Integer.MAX_VALUE
 */
    //
    // start the VM with e.g. -Dlog4j.configurationFile=file:/c:/projects/ben/log4j2.properties
    // or just place an file log4j2.xml in the classpath
    //
    //
    public static void main(String[] args) {
        //System.out.println(System.getProperties());

        try {
            log.fatal("fatal");
            log.error("error");
            log.warn("warn");
            log.info("info");
            log.debug("debugging");
            log.trace("trace");

            ClassWithMarkers cwm = new ClassWithMarkers();
            cwm.logTest();
            cwm.calcTheOtherThing();

            AnotherClassWithMarkers acwm = new AnotherClassWithMarkers();
            acwm.logTest();
            acwm.calc();

            TraceThisClass ttc = new TraceThisClass();
            ttc.tracedMethod(5);
            ttc.tracedMethod(10000);

        } catch (Exception e) {
            log.catching(e);
        }


        LogManager.shutdown();
    }
}
