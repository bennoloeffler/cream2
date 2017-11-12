package bel.learn._05_nio;

import bel.learn._14_timingExecution.RunTimer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;

/**
 * http://tutorials.jenkov.com/java-nio/asynchronousfilechannel.html
 */
public class AsyncReadingAndWriting {
    public static void main(String[] args) throws Exception{

        //
        // read async
        //
        Path path = Paths.get("C:\\temp\\Adobe Premiere Elements 12\\PRE 12\\data16.cab");

        AsynchronousFileChannel fileChannel =
                AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(1000000000);
        long position = 0;
        RunTimer t = new RunTimer();
        Future<Integer> operation = fileChannel.read(buffer, position);

        while(!operation.isDone()) {
            sleep(1);
            System.out.print(".");
        }
        t.stop("reading file async");

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        //System.out.println(new String(data));
        buffer.clear();


    }
}
