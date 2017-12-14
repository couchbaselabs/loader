package com.couchbase.bigfun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.IOException;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultFile implements Closeable {
    private PrintStream stream = null;

    private String getCurrentTime() {
        Date d = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return sf.format(d);
    }

    public void printResult(String result) {
        System.out.println(this.getCurrentTime() + " " + result);
        if (stream != null) {
            stream.println(result);
        }
        return;
    }

    public ResultFile() {
        this(null);
    }

    public ResultFile(String filename) {
        if (filename != null) {
            File file = new File(filename);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                stream = new PrintStream(new File(filename));
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid log file " + filename, e);
            }
        }
    }

    public void close() throws IOException {
        if (stream != null)
            stream.close();
    }
}
