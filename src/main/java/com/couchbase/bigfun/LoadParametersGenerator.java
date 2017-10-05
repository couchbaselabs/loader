package com.couchbase.bigfun;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class LoadParametersGenerator {

    protected HashMap<String,Object> arguments = new HashMap<String,Object>();
    protected String args[];

    public LoadParametersGenerator(String args[]) {
        this.args = args;
    }

    protected abstract void run();

    protected void PrintOptionsUsage() {
        System.out.println("-P <partitionpath>");
        System.out.println("-d <datafile>");
        System.out.println("-k <keyfield>");
        System.out.println("-l <doctoload>");
        System.out.println("-h <host>");
        System.out.println("-u <username>");
        System.out.println("-p <password>");
        System.out.println("-b <bucket>");
        System.out.println("-ah <cbashost>");
        System.out.println("-qf <queryfile>");
    }

    protected void parseArguments() {
        int i = 0;
        while (i < args.length) {

            String arg = args[i];

            if (arg.startsWith("-")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for option " + arg);
                }

                switch (arg) {
                    case "-P":
                        arguments.put("partitionPath", args[++i]);
                        break;
                    case "-d":
                        arguments.put("dataFile", args[++i]);
                        break;
                    case "-l":
                        arguments.put("docToLoad", Long.valueOf(args[++i]));
                        break;
                    case "-h":
                        arguments.put("host", args[++i]);
                        break;
                    case "-u":
                        arguments.put("user", args[++i]);
                        break;
                    case "-p":
                        arguments.put("pwd", args[++i]);
                        break;
                    case "-b":
                        arguments.put("bucket", args[++i]);
                        break;
                    case "-k":
                        arguments.put("keyField", args[++i]);
                        break;
                    case "-ah":
                        arguments.put("cbasHost", args[++i]);
                        break;
                    case "-qf":
                        arguments.put("queryFile", args[++i]);
                        break;
                }
            }
            ++i;
        }

        return;
    }

    protected static void parseTTLArg(HashMap<String,Object> arguments, String arg) {
        String parts[] = arg.split("#");
        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid TTL parameter " + arg);
        try {
            int ttlStart = Integer.valueOf(parts[0]);
            int ttlEnd = Integer.valueOf(parts[1]);
            arguments.put("ttlStart", ttlStart);
            arguments.put("ttlEnd", ttlEnd);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid TTL parameter " + arg, e);
        }
        return;
    }

    protected static void parseTime(HashMap<String,Object> arguments, String name, String arg) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            arguments.put(name, formatter.parse(arg));
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("Invalid time argument " + arg, e);
        }
        return;
    }

    protected QueryInfo getQueryInfo() {
        QueryInfo queryInfo = new QueryInfo();
        if (arguments.containsKey("queryFile")) {
            try {
                String filename = (String) arguments.get("queryFile");
                List<String> querylist = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
                for (String query : querylist) {
                    queryInfo.queries.add(query);
                }
            }
            catch (IOException e) {
                throw new IllegalArgumentException("IOException when reading query file", e);
            }
        }
        return queryInfo;
    }

    protected DataInfo[] getAllPartitionDataInfos() {
        String allPartitions[] = getAllSubFolders((String) arguments.get("partitionPath"));
        DataInfo dataInfos[] = new DataInfo[allPartitions.length];
        for (int i = 0; i < allPartitions.length; i++) {
            File dataFile = new File(allPartitions[i], (String) arguments.get("dataFile") + ".json");
            File metaFile = new File(allPartitions[i], (String) arguments.get("dataFile") + ".meta");
            if (!dataFile.exists() || !dataFile.isFile())
                throw new IllegalArgumentException("Invalid partition data file " + dataFile.getAbsolutePath());
            if (!metaFile.exists() || !metaFile.isFile())
                throw new IllegalArgumentException("Invalid partition meta file " + metaFile.getAbsolutePath());
            dataInfos[i] = new DataInfo(dataFile.getAbsolutePath(), metaFile.getAbsolutePath(),
                    (String) arguments.get("keyField"), (long) arguments.get("docToLoad"));
        }
        return dataInfos;
    }

    protected TargetInfo getTargetInfo() {
        String cbasHost = "";
        if (arguments.containsKey("cbasHost"))
            cbasHost = (String)arguments.get("cbasHost");
        return new TargetInfo((String) arguments.get("host"), (String) arguments.get("bucket"),
                (String) arguments.get("user"), (String) arguments.get("pwd"), cbasHost);
    }

    private static String[] getAllSubFolders(String partitionPath)
    {
        File file = new File(partitionPath);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        Arrays.sort(directories);
        for (int i = 0; i < directories.length; i ++)
        {
            directories[i] = new File(file, directories[i]).getAbsolutePath();
        }
        return directories;
    }
}
