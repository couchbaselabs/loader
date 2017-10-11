package com.couchbase.bigfun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.IOException;
import java.io.Closeable;

public class ResultFile implements Closeable {
    private PrintStream stream = null;

    public void printResult(String result) {
        if (stream == null) {
            System.out.println(result);
        } else {
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
