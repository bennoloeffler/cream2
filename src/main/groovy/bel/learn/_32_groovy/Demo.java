package bel.learn._32_groovy;


/**
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan
 * @version $Revision: 20587 $
 */
public class Demo { //extends GroovyTestCase {
    /*
    ClassLoader parentLoader = getClass().getClassLoader();
    protected GroovyClassLoader loader =
            (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return new GroovyClassLoader(parentLoader);
                }
            });

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        GroovyObject object = demo.compile("src/bel/SwingDemo.groovy");
        object.invokeMethod("run", null);
    }

    protected GroovyObject compile(String fileName) throws Exception {
        Class groovyClass = loader.parseClass(new GroovyCodeSource(new File(fileName)));
        GroovyObject object = (GroovyObject) groovyClass.newInstance();
        assertTrue(object != null);
        return object;
    }
    */
}
