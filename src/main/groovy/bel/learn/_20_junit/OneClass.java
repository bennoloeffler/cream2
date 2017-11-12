package bel.learn._20_junit;

/**
 * 0) Include junit4.xx.jar in the classpath
 * 1) Create a test package parallel to src and tell intellij, that this is a test package
 * 2) In Class-Context-Menu choose "goto". This will eiter navigat to or create a Test class.
 * 3) Name the Test class OneClassToTestTest (Test at the end). This will sort correctly.
 * 4) MAYBE: create a test suite, that collects all the test cases - to be able to run all...
 * 5) or just run all the tests from within context menu of intellij
 *
 * intellij: Toggle between Test and Subject STRG-SHIFT-T
 *
 * Create a test by annotation: @Test
 *
 * hamcrest XXX.jar lib is needed in addition to junit XXX.jar
 *
 *

 Annotations:

 @Test public void method()	The annotation @Test identifies that a method is a test method.
 @Before public void method()	Will execute the method before each test. This method can prepare the test environment (e.g. read input data, initialize the class).
 @After public void method()	Will execute the method after each test. This method can cleanup the test environment (e.g. delete temporary data, restore defaults).
 @BeforeClass public void method()	Will execute the method once, before the start of all tests. This can be used to perform time intensive activities, for example to connect to a database.
 @AfterClass public void method()	Will execute the method once, after all tests have finished. This can be used to perform clean-up activities, for example to disconnect from a database.
 @Ignore	Will ignore the test method. This is useful when the underlying code has been changed and the test case has not yet been adapted. Or if the execution time of this test is too long to be included.
 @Test (expected = Exception.class)	Fails, if the method does not throw the named exception.
 @Test(timeout=100)	Fails, if the method takes longer than 100 milliseconds.


 Statements for testing:

 fail(String)	Let the method fail. Might be used to check that a certain part of the code is not reached. Or to have failing test before the test code is implemented.
 assertTrue(true) / assertTrue(false)	Will always be true / false. Can be used to predefine a test result, if the test is not yet implemented.
 assertTrue([message], boolean condition)	Checks that the boolean condition is true.
 assertsEquals([String message], expected, actual)	Tests that two values are the same. Note: for arrays the reference is checked not the content of the arrays.
 assertsEquals([String message], expected, actual, tolerance)	Test that float or double values match. The tolerance is the number of decimals which must be the same.
 assertNull([message], object)	Checks that the object is null.
 assertNotNull([message], object)	Checks that the object is not null.
 assertSame([String], expected, actual)	Checks that both variables refer to the same object.
 assertNotSame([String], expected, actual)

 *
 */
public class OneClass {
   public String doSomthing() {
       return "something";
   }
}

/**
 * the test looks like this
 */
/*
public class OneClassTest {

    OneClass one;

        @Before
        public void createIt() {
            one = new OneClass();
        }

        @Test
        public  void doSomethingTest() {
            assertEquals(one.doSomthing(), "doSomething");
        }
}
*/
