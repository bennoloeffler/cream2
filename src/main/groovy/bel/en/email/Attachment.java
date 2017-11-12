package bel.en.email;

import java.io.Serializable;

/**
 * Attachment represents an email attachment
 */
public class Attachment implements Serializable {
    public static long serialver = 1; // TODO: provide right one
    String fileName;
    byte[] data;

    public Attachment(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

}
