package com.couchbase.bigfun;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.error.TemporaryFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Random;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.format.DateTimeFormatter;


public class Operation {

    String operation;

    String updatefieldname;
    String updatevaluetype;
    String updatevalueformat = null;
    ArrayList<String> updatevalues = new ArrayList<String>();
    String updatevaluestart;
    String updatevalueend;

    String ttlstart;
    String ttlend;
    Random random = new Random();

    boolean operate(Bucket bucket, JsonDocument doc, long timeout) throws InterruptedException, ParseException {
        try {
            if (this.operation == "insert")
                bucket.upsert(doc, PersistTo.NONE, timeout, TimeUnit.MILLISECONDS);
            else if (this.operation == "delete")
                bucket.remove(doc, PersistTo.NONE, timeout, TimeUnit.MILLISECONDS);
            else if (this.operation == "update") {
                this.generateUpdateValue(doc);
                bucket.upsert(doc, PersistTo.NONE, timeout, TimeUnit.MILLISECONDS);
            }
            else if (this.operation == "ttl") {
                int s = Integer.parseInt(this.ttlstart);
                int e = Integer.parseInt(this.ttlend);
                int v = s + (int) (random.nextDouble() * (e - s));
                JsonDocument newdoc = JsonDocument.create(doc.id(), v, doc.content());
                bucket.upsert(doc, PersistTo.NONE, timeout, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (RuntimeException e) {
            if (e instanceof TemporaryFailureException || e.getCause() instanceof TimeoutException) {
                System.out.println();
                System.out.println("+++ caught " + e.toString() + " +++");
                Thread.sleep(timeout);
                timeout *= 2;
                System.out.println("+++ new timeout " + timeout + " +++");
            } else {
                throw e;
            }
        }
        return false;
    }

    void generateUpdateValue(JsonDocument doc) throws ParseException {
        switch (this.updatevaluetype) {
            case "i": {
                Long s = Long.parseLong(this.updatevaluestart);
                Long e = Long.parseLong(this.updatevalueend);
                Long v = s + (long) (random.nextDouble() * (e - s));
                doc.content().put(this.updatefieldname, v);
                break;
            }
            case "f": {
                Double s = Double.parseDouble(this.updatevaluestart);
                Double e = Double.parseDouble(this.updatevalueend);
                Double v = s + random.nextDouble() * (e - s);
                doc.content().put(this.updatefieldname, v);
                break;
            }
            case "d": {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Long s = formatter.parse(this.updatevaluestart).getTime();
                Long e = formatter.parse(this.updatevalueend).getTime();
                Long msv = s + (long) (random.nextDouble() * (e - s));
                String v = formatter.format(new Date(msv));
                doc.content().put(this.updatefieldname, v);
                break;
            }
            case "t": {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Long s = formatter.parse(this.updatevaluestart).getTime();
                Long e = formatter.parse(this.updatevalueend).getTime();
                Long msv = s + (long) (random.nextDouble() * (e - s));
                String v = formatter.format(new Date(msv));
                doc.content().put(this.updatefieldname, v);
                break;
            }
            case "s": {
                if (updatevalueformat != null) {
                    Long s = Long.parseLong(this.updatevaluestart);
                    Long e = Long.parseLong(this.updatevalueend);
                    Long v = s + (long) (random.nextDouble() * (e - s));
                    String sv = String.format(this.updatevalueformat, v);
                    doc.content().put(this.updatefieldname, sv);
                }
                else if (this.updatevalues.size() > 0) {
                    int vidx = random.nextInt(this.updatevalues.size());
                    String v = this.updatevalues.get(vidx);
                    doc.content().put(this.updatefieldname, v);
                }
                break;
            }
        }
    }

    Operation(String _operation, String _operationparams) throws FileNotFoundException, IOException {
        this.operation = _operation;
        if (this.operation == "update") {
            String[] parts = _operationparams.split("#");
            if (parts.length >= 3) {
                updatefieldname = parts[0];
                updatevaluetype = parts[1];
                System.out.println(parts[0]);
                System.out.println(parts[1]);
                System.out.println(parts[2]);
                if (updatevaluetype == "s") {
                    if (parts.length == 3) {
                        String filename = parts[2];
                        final File file = new File(filename);
                        if (!file.exists()) {
                            throw new FileNotFoundException(file.getAbsolutePath());
                        }
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        for (String line; (line = br.readLine()) != null; ) {
                            updatevalues.add(line);
                        }
                    }
                    if (parts.length == 5) {
                        updatevalueformat = parts[2];
                        updatevaluestart = parts[3];
                        updatevalueend = parts[4];
                    }
                } else {
                    updatevaluestart = parts[2];
                    updatevalueend = parts[3];
                }
            }
        }
        else if (this.operation == "ttl") {
            String[] parts = _operationparams.split("#");
            if (parts.length >= 2) {
                ttlstart = parts[0];
                ttlend = parts[1];
            }
        }
    }
}
