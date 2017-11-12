package bel.en.email;

import java.io.*;

/**
 * Just increments a counter a number and stores the value in a file.
 */
public class PersistentCounter {
    private String fileName;

    public PersistentCounter(String fileName) {
        this(fileName, 0);
    }

    public PersistentCounter(String fileName, long initialValue) {
        this.fileName = fileName;
        File f = new File(fileName);
        if (!f.exists()) {
            initCounter(initialValue);
        }
    }

    /**
     * Set the counter to a new value and create the file, if neccesary.
     *
     * @param initialValue
     */
    synchronized public void initCounter(long initialValue) {
        try {
            File f = new File(fileName);
            File path = f.getAbsoluteFile();
            path = path.getParentFile();
            path.mkdirs(); // if the path does not yet exist, just create it
            f.createNewFile();
            writeValue(initialValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Add one to the current counter.
     */
    synchronized public void incrementCurrentCount() {
        long current = getCurrentCount();
        writeValue(++current);
    }


    /**
     * Write the value <code>counter</code> to the file.
     *
     * @param counter
     */
    private void writeValue(long counter) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(fileName));
            pw.println(counter);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }


    /**
     * @return current counter value
     */
    synchronized public long getCurrentCount() {
        BufferedReader dis = null;
        try {
            dis = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String counterStr = dis.readLine();
            long counter = Long.parseLong(counterStr);
            return counter;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
