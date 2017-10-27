package com.couchbase.bigfun;

import com.couchbase.client.java.document.json.JsonObject;

import java.text.ParseException;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.io.*;

public class JsonObjectUpdater {
    public final static String INTEGER_TYPE = "integer";
    public final static String FLOAT_TYPE = "float";
    public final static String STRING_TYPE = "string";
    public final static String DATE_TYPE = "date";
    public final static String TIME_TYPE = "time";
    private BatchModeUpdateParameter updateParameter;
    private ArrayList<String> updateValues = new ArrayList<String>();
    private Random random = new Random();
    public void updateJsonObject(JsonObject obj) {
        try {
            switch (this.updateParameter.updateFieldType) {
                case INTEGER_TYPE: {
                    long s = Long.parseLong(this.updateParameter.updateValueStart);
                    long e = Long.parseLong(this.updateParameter.updateValueEnd);
                    long v = s + (long) (random.nextDouble() * (e - s));
                    obj.put(this.updateParameter.updateFieldName, v);
                    break;
                }
                case FLOAT_TYPE: {
                    Double s = Double.parseDouble(this.updateParameter.updateValueStart);
                    Double e = Double.parseDouble(this.updateParameter.updateValueEnd);
                    Double v = s + random.nextDouble() * (e - s);
                    obj.put(this.updateParameter.updateFieldName, v);
                    break;
                }
                case DATE_TYPE: {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    long s = formatter.parse(this.updateParameter.updateValueStart).getTime();
                    long e = formatter.parse(this.updateParameter.updateValueEnd).getTime();
                    long msv = s + (long) (random.nextDouble() * (e - s));
                    String v = formatter.format(new Date(msv));
                    obj.put(this.updateParameter.updateFieldName, v);
                    break;
                }
                case TIME_TYPE: {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    long s = formatter.parse(this.updateParameter.updateValueStart).getTime();
                    long e = formatter.parse(this.updateParameter.updateValueEnd).getTime();
                    long msv = s + (long) (random.nextDouble() * (e - s));
                    String v = formatter.format(new Date(msv));
                    obj.put(this.updateParameter.updateFieldName, v);
                    break;
                }
                case STRING_TYPE: {
                    if (this.updateValues.size() > 0) {
                        int vidx = random.nextInt(this.updateValues.size());
                        String v = this.updateValues.get(vidx);
                        obj.put(this.updateParameter.updateFieldName, v);
                    } else if (this.updateParameter.updateValueFormat != null && (!this.updateParameter.updateValueFormat.equals(""))) {
                        long s = Long.parseLong(this.updateParameter.updateValueStart);
                        long e = Long.parseLong(this.updateParameter.updateValueEnd);
                        long v = s + (long) (random.nextDouble() * (e - s));
                        obj.put(this.updateParameter.updateFieldName, String.format(this.updateParameter.updateValueFormat, v));
                    }
                    break;
                }
            }
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Parsing exception when updating json object");
        }
        return;
    }

    public JsonObjectUpdater(BatchModeUpdateParameter updateParameter) {
        this.updateParameter = updateParameter;
        if (this.updateParameter.updateValuesFilePath != null && (!this.updateParameter.updateValuesFilePath.equals(""))) {
            File file = new File(this.updateParameter.updateValuesFilePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("Invalid value file path " + file.getAbsolutePath());
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                for (String line; (line = br.readLine()) != null; ) {
                    updateValues.add(line);
                }
            }
            catch (IOException e) {
                throw new IllegalArgumentException("Invalid value file " + file.getAbsolutePath());
            }
        }
        return;
    }
}
