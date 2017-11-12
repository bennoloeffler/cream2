package bel.en.test;

//import bel.FooBar;
import bel.util.Util;
import junit.framework.TestCase;


public class UtilTest extends TestCase {

    public void testReadableTime() {
        String t = Util.readableTime(1000 * 60 * 3 + 1000 * 5 + 777);
        assertEquals("min: 3, sec: 5, ms: 777", t);
    }

    /*
    public void testGroovyCall() {
        FooBar fb = new FooBar();
        System.out.println(fb);
    }*/
}