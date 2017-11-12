package bel.learn._00_logging.package2;

import lombok.extern.log4j.Log4j2;

import static bel.learn._00_logging.package2.AnotherClassWithMarkers.CALC_MARKER;

/**
 * demonstrate markers
 */
@Log4j2
public class ClassWithMarkers {
    public void logTest() {
        log.fatal("fatal");
        log.error("error");
        log.warn("warn");
        log.info("info");
        log.debug("debugging");
        log.trace("trace");
    }

    public void calcTheOtherThing() {
        int x =88;
        log.debug(CALC_MARKER,"calc info: {}", x);
        log.debug("calc info THIS IS NOT SHOWN: {}", x);
    }
}
