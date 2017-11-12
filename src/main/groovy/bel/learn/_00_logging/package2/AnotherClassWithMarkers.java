package bel.learn._00_logging.package2;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Show markers
 */
@Log4j2
public class AnotherClassWithMarkers {

    public static final Marker CALC_MARKER = MarkerManager.getMarker("CALC_MARKER");

    public void logTest() {
        log.fatal("fatal");
        log.error("error");
        log.warn("warn");
        log.info("info");
        log.debug("debugging");
        log.trace("trace");
    }

    public void calc() {
        int x =77;
        log.debug(CALC_MARKER,"calc info: {}", x);
        log.debug("calc info THIS IS NOT SHOWN: {}", x);
    }

}
