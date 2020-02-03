package edu.indiana.dlib.amppd.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * An IO utility class which consumes an InputStream.
 * @author yingfeng
 *
 */
public class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;
 
    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }
 
    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines()
          .forEach(consumer);
    }
}
