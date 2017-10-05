package com.couchbase.bigfun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.io.File;
import java.io.FilenameFilter;

public class BatchModeLoadParametersGeneratorEntry extends LoadParametersGenerator {

    public BatchModeLoadParametersGeneratorEntry(String args[]) {
        super(args);
    }

    private void Usage() {
        System.out.println("BatchModeLoadParametersGeneratorEntry [Option]+");
        System.out.println("Options:");
        super.PrintOptionsUsage();
        System.out.println("-o <operation>");
        System.out.println("-t <ttlstart>#<ttlend>");
        System.out.println("-U <fieldname>#<fieldtype>#<valuestart>#<valueend>");
        System.out.println("-U <fieldname>#<fieldtype>#<valueformat>#<valuestart>#<valueend>");
        System.out.println("-U <fieldname>#<fieldtype>#<valuesfile>");
        System.exit(1);
    }

    private static void parseUpdateArg(HashMap<String,Object> arguments, String arg) {
        String parts[] = arg.split("#");
        if (parts.length <= 2)
            throw new IllegalArgumentException("Invalid update parameter " + arg);
        try {
            arguments.put("fieldName", parts[0]);
            arguments.put("fieldType", parts[1]);
            if (parts.length == 3) {
                arguments.put("valuesFile", parts[2]);
            }
            else if (parts.length == 4) {
                arguments.put("valueStart", parts[2]);
                arguments.put("valueEnd", parts[3]);
            }
            else if (parts.length == 5) {
                arguments.put("valueFormat", parts[2]);
                arguments.put("valueStart", parts[3]);
                arguments.put("valueEnd", parts[4]);
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid update parameter " + arg, e);
        }
        return;
    }

    protected void parseArguments() {
        super.parseArguments();
        {
            int i = 0;
            while (i < args.length) {

                String arg = args[i];

                if (arg.startsWith("-")) {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for option " + arg);
                    }

                    switch (arg) {
                        case "-o":
                            arguments.put("operation", args[++i]);
                            break;
                        case "-t":
                            parseTTLArg(arguments, args[++i]);
                            break;
                        case "-U":
                            parseUpdateArg(arguments, args[++i]);
                            break;
                    }
                }
                ++i;
            }
        }
        return;
    }

    public static void main(String[] args) {
        BatchModeLoadParametersGeneratorEntry entry = new BatchModeLoadParametersGeneratorEntry(args);
        entry.run();
        return;
    }

    private BatchModeTTLParameter getTTLParameter() {
        return new BatchModeTTLParameter((int) arguments.get("ttlStart"), (int) arguments.get("ttlEnd"));
    }

    private BatchModeUpdateParameter getUpdateParameter() {
        BatchModeUpdateParameter updateParameter = null;
        if (arguments.containsKey("valueFormat")) {
            updateParameter = new BatchModeUpdateParameter((String) arguments.get("fieldName"), (String) arguments.get("fieldType"),
                    (String) arguments.get("valueFormat"), (String) arguments.get("valueStart"), (String) arguments.get("valueEnd"));
        } else if (arguments.containsKey("valuesFile")) {
            updateParameter = new BatchModeUpdateParameter((String) arguments.get("fieldName"), (String) arguments.get("fieldType"),
                    (String) arguments.get("valuesFile"));
        } else {
            updateParameter = new BatchModeUpdateParameter((String) arguments.get("fieldName"), (String) arguments.get("fieldType"),
                    (String) arguments.get("valueStart"), (String) arguments.get("valueEnd"));
        }
        return updateParameter;
    }

    @Override
    public void run() {
        try {
            parseArguments();
            if (arguments == null || arguments.size() < 1) {
                Usage();
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.toString());
            Usage();
        }

        DataInfo allPartitionDataInfos[] = getAllPartitionDataInfos();
        TargetInfo targetInfo = getTargetInfo();
        QueryInfo queryInfo = getQueryInfo();
        BatchModeUpdateParameter updateParameter = getUpdateParameter();
        BatchModeTTLParameter ttlParameter = getTTLParameter();
        BatchModeLoadParameters batchModeLoadParameters = new BatchModeLoadParameters();
        for (int i = 0; i < allPartitionDataInfos.length; i++) {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(
                    allPartitionDataInfos[i], targetInfo, queryInfo,
                    (String) arguments.get("operation"), updateParameter, ttlParameter);
            batchModeLoadParameters.loadParameters.add(batchModeLoadParameter);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String parametersJson = gson.toJson(batchModeLoadParameters);
        System.out.println(parametersJson);
        return;
    }
}
