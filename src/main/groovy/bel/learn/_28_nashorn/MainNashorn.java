package bel.learn._28_nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @see <></>http://winterbe.com/posts/2014/04/05/java8-nashorn-tutorial/
 */
public class MainNashorn {
    public static void main(String[] args) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval("print('Hello World!');");
    }
}
