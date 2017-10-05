package com.couchbase.bigfun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Date;

public class MixModeLoadParametersGeneratorEntry extends LoadParametersGenerator {

    public MixModeLoadParametersGeneratorEntry(String args[]) {
        super(args);
    }

    private void Usage() {
        System.out.println("MixModeLoadParametersGeneratorEntry [Option]+");
        System.out.println("Options:");
        super.PrintOptionsUsage();
        System.out.println("-t <ttlstart>#<ttlend>");
        System.out.println("-iv <intervalms>");
        System.out.println("-du <durationsec>");
        System.out.println("-st <starttime>");
        System.out.println("-ip <insertportion>");
        System.out.println("-dp <deleteportion>");
        System.out.println("-up <upateportion>");
        System.out.println("-tp <ttlportion>");
        System.out.println("-qp <queryportion>");
        System.out.println("-md <maxdeletedocs>");
        System.out.println("-is <insertstart>");
        System.out.println("-ir <insertrange>");
        System.exit(1);
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
                        case "-t":
                            parseTTLArg(arguments, args[++i]);
                            break;
                        case "-iv":
                            arguments.put("intervalMS", Integer.valueOf(args[++i]));
                            break;
                        case "-du":
                            arguments.put("durationSeconds", Integer.valueOf(args[++i]));
                            break;
                        case "-st":
                            parseTime(arguments, "startTime", args[++i]);
                            break;
                        case "-ip":
                            arguments.put("insertPropotion", Integer.valueOf(args[++i]));
                            break;
                        case "-dp":
                            arguments.put("deletePropotion", Integer.valueOf(args[++i]));
                            break;
                        case "-up":
                            arguments.put("updatePropotion", Integer.valueOf(args[++i]));
                            break;
                        case "-tp":
                            arguments.put("ttlPropotion", Integer.valueOf(args[++i]));
                            break;
                        case "-qp":
                            arguments.put("queryPropotion", Integer.valueOf(args[++i]));
                            break;
                        case "-md":
                            arguments.put("maxDeleteDocs", Long.valueOf(args[++i]));
                            break;
                        case "-is":
                            arguments.put("insertIdStart", Long.valueOf(args[++i]));
                            break;
                        case "-ir":
                            arguments.put("insertIdRange", Long.valueOf(args[++i]));
                            break;
                    }
                }
                ++i;
            }
        }
        return;
    }

    public static void main(String[] args) {
        MixModeLoadParametersGeneratorEntry entry = new MixModeLoadParametersGeneratorEntry(args);
        entry.run();
        return;
    }

    private MixModeTTLParameter getTTLParameter() {
        return new MixModeTTLParameter((int) arguments.get("ttlStart"), (int) arguments.get("ttlEnd"));
    }

    private MixModeDeleteParameter getDeleteParameter() {
        return new MixModeDeleteParameter((long) arguments.get("maxDeleteDocs"));
    }

    private MixModeInsertParameter getInsertParameter(int partitionid) {
        return new MixModeInsertParameter((long) arguments.get("insertIdStart") + partitionid * (long)arguments.get("insertIdRange"));
    }

    private Date getStartTime() {
        Date startTime = new Date();
        if (arguments.containsKey("startTime")) {
            startTime = (Date) arguments.get("startTime");
        }
        return startTime;
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

        TargetInfo targetInfo = getTargetInfo();
        DataInfo partitionDataInfos[] = getAllPartitionDataInfos();
        QueryInfo queryInfo = getQueryInfo();
        MixModeTTLParameter ttlParameter = getTTLParameter();
        MixModeDeleteParameter deleteParameter = getDeleteParameter();
        Date startTime = getStartTime();
        MixModeLoadParameters mixModeLoadParameters = new MixModeLoadParameters();
        for (int i = 0; i < partitionDataInfos.length; i++) {
            MixModeInsertParameter insertParameter = getInsertParameter(i);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(
                    (int)arguments.get("intervalMS"), (int)arguments.get("durationSeconds"), startTime,
                    (int)arguments.get("insertPropotion"), (int)arguments.get("updatePropotion"),
                    (int)arguments.get("deletePropotion"), (int)arguments.get("ttlPropotion"), (int)arguments.get("queryPropotion"),
                    partitionDataInfos[i], targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            mixModeLoadParameters.loadParameters.add(mixModeLoadParameter);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String parametersJson = gson.toJson(mixModeLoadParameters);
        System.out.println(parametersJson);
        return;
    }
}
