package com.couchbase.bigfun;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

import java.io.*;

import java.util.ArrayList;
import java.util.Random;

import java.io.BufferedReader;

import java.util.HashSet;

import java.util.regex.Pattern;

import java.util.regex.Matcher;

public class MixModeLoadData extends LoadData {

    private MixModeInsertParameter insertParam;
    private MixModeDeleteParameter delParam;
    private MixModeTTLParameter ttlParameter;

    private long operationIdStart;
    private long operationIdEnd;

    private long extraInsertIdStart;
    private long currentExtraInsertId;
    public long getCurrentExtraInsertId() {return this.currentExtraInsertId;};

    private JsonDocument templateDocuments[];

    private long deletedDocNumberThreshold;

    private int expiryStart;
    private int expiryEnd;

    private HashSet<String> removedKeys = new HashSet<String>();

    private ArrayList<String> queries = new ArrayList<String>();

    public long getRemovedKeysNumber() {return this.removedKeys.size();}

    private Random random = new Random();

    private String getRandomNonRemovedKey() {
        while (true) {
            String key = String.valueOf(operationIdStart + (long) (random.nextDouble() * (operationIdEnd - operationIdStart)));
            if (removedKeys.contains(key)) {
                continue;
            }
            return key;
        }
    }

    private String getRandomKeyToInsert() {
        if (removedKeys.size() >= this.deletedDocNumberThreshold) {
            String k = this.removedKeys.iterator().next();
            removedKeys.remove(k);
            return k;
        }
        else
            return String.valueOf(currentExtraInsertId++);
    }

    private String getRandomKeyToRemove() {
        if (removedKeys.size() >= ((operationIdEnd - operationIdStart + 1) / 2))
            throw new RuntimeException("Too much documents removed");
        String k = getRandomNonRemovedKey();
        removedKeys.add(k);
        return k;
    }

    private int getRandomExpiry() {
        return this.expiryStart + random.nextInt(this.expiryEnd - this.expiryStart);
    }

    private JsonDocument getRandomDocumentTemplate() {
        int docIdx = random.nextInt(templateDocuments.length);
        return this.templateDocuments[docIdx];
    }

    @Override
    public JsonDocument GetNextDocumentForUpdate() {
        String keyToUpdate = getRandomNonRemovedKey();
        JsonDocument docTemplate = getRandomDocumentTemplate();
        return JsonDocument.create(keyToUpdate, docTemplate.content());
    }

    @Override
    public JsonDocument GetNextDocumentForDelete() {
        String keyToDelete = getRandomKeyToRemove();
        JsonDocument docTemplate = getRandomDocumentTemplate();
        return JsonDocument.create(keyToDelete, docTemplate.content());
    }

    @Override
    public JsonDocument GetNextDocumentForInsert() {
        String keyToInsert = getRandomKeyToInsert();
        JsonDocument docTemplate = getRandomDocumentTemplate();
        return JsonDocument.create(keyToInsert, docTemplate.content());
    }

    @Override
    public JsonDocument GetNextDocumentForTTL() {
        String keyToTTL = getRandomKeyToRemove();
        JsonDocument docTemplate = getRandomDocumentTemplate();
        int expiry = getRandomExpiry();
        return JsonDocument.create(keyToTTL, expiry, docTemplate.content());
    }

    @Override
    public String GetNextQuery() {
        String query = null;
        if (this.queries.size() > 0) {
            int qidx = random.nextInt(this.queries.size());
            query = this.queries.get(qidx);
        }
        return query;
    }

    @Override
    public void close() {
        return;
    }

    public MixModeLoadData(DataInfo dataInfo, QueryInfo queryInfo, MixModeInsertParameter insertParam,
                           MixModeDeleteParameter delParam, MixModeTTLParameter ttlParameter) {
        super(dataInfo, queryInfo);
        this.insertParam = insertParam;
        this.delParam = delParam;
        this.ttlParameter = ttlParameter;

        this.extraInsertIdStart = this.insertParam.insertIdStart;
        this.currentExtraInsertId = extraInsertIdStart;

        this.deletedDocNumberThreshold = this.delParam.maxDeleteIds;

        this.expiryStart = this.ttlParameter.expiryStart;
        this.expiryEnd = this.ttlParameter.expiryEnd;

        try {
            BufferedReader br = new BufferedReader(new FileReader(this.dataInfo.metaFilePath));
            String line = br.readLine();
            String patternString = "IDRange=([0-9]+):([0-9]+)";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(line);
            String idStartString = null;
            String idEndString = null;
            while(matcher.find()) {
                idStartString = matcher.group(1);
                idEndString = matcher.group(2);
            }
            if (idStartString == null || idEndString == null)
                throw new IllegalArgumentException("Invalid meta file content");
            this.operationIdStart = Long.valueOf(idStartString);
            this.operationIdEnd = Long.valueOf(idEndString);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Invalid meta file path");
        }

        try {
            this.templateDocuments = new JsonDocument[(int)dataInfo.docsToLoad];
            BufferedReader br = new BufferedReader(new FileReader(this.dataInfo.dataFilePath));
            for (int i = 0; i < this.dataInfo.docsToLoad; i++) {
                String line = br.readLine();
                if (line == null)
                    break;
                JsonObject obj = JsonObject.fromJson(line);
                String id = String.valueOf(obj.get(this.dataInfo.keyFieldName));
                this.templateDocuments[i] = JsonDocument.create(id, obj);
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Invalid data file path");
        }

        for (String query : queryInfo.queries) {
            this.queries.add(query);
        }
    }
}
