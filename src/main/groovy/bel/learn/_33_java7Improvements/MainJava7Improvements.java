package bel.learn._33_java7Improvements;

import java.io.IOException;

/**
 * Created 07.05.2017.
 */
public class MainJava7Improvements {
    public static void main(String[] args) throws IOException { // Java 7 lets us declare the precise one...
        int i = 1234_5678; // long numbers... // java 7 letzs us do this here 1234_1234. just to make it more readable.

        print(i,2,3,4);

        try {
            writeToFileZipFileContents("BELsTestZipFile.zip", "BELsResult.txt");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        String decision = "Benno";
        switch (decision) { // java 7 lets us do switch on Strings
            case "Benno": break;
            case "Sabine": return;
        }

    }

    @SafeVarargs // java 7 lets us suppress warnings
    // WARNING SUPPRESSED: Type safety: Potential heap pollution via varargs parameter a
    public static <T> void print(T... a) {
        for (T t : a) {
            System.out.println(t);
        }
    }

    public static void writeToFileZipFileContents(String zipFileName,
                                                  String outputFileName)
            throws java.io.IOException {

        java.nio.charset.Charset charset =
                java.nio.charset.StandardCharsets.US_ASCII;
        java.nio.file.Path outputFilePath =
                java.nio.file.Paths.get(outputFileName);

        // Open zip file and create output file with
        // try-with-resources statement

        try ( // java 7 lets us do this and guarantees, that ressources are closed
                java.util.zip.ZipFile zf =
                        new java.util.zip.ZipFile(zipFileName);
                java.io.BufferedWriter writer =
                        java.nio.file.Files.newBufferedWriter(outputFilePath, charset)
        ) {
            // Enumerate each entry
            for (java.util.Enumeration entries =
                 zf.entries(); entries.hasMoreElements();) {
                // Get the entry name and write it to the output file
                String newLine = System.getProperty("line.separator");
                String zipEntryName =
                        ((java.util.zip.ZipEntry)entries.nextElement()).getName() +
                                newLine;
                writer.write(zipEntryName, 0, zipEntryName.length());
            }
        }
    }
}
