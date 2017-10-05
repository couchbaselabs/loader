package com.couchbase.bigfun;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

import java.io.*;
import java.util.Random;
import java.util.ArrayList;

public class BatchModeLoadData extends LoadData {

    private BatchModeTTLParameter ttlParam;
    private BatchModeUpdateParameter updateParam;

    private BufferedReader bufferedReader;
    private String keyFieldName;
    private long maxDocumentsToLoad;
    private long loadedDocuments;

    private int expiryStart;
    private int expiryEnd;

    private ArrayList<String> queries = new ArrayList<String>();
    private int currentQueryIndex;

    private JsonObjectUpdater updater;

    private Random random = new Random();

    private JsonDocument GetNextDocument() {
        JsonDocument result = null;
        try {
            if (this.maxDocumentsToLoad == 0 || loadedDocuments < this.maxDocumentsToLoad) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    JsonObject obj = JsonObject.fromJson(line);
                    String id = String.valueOf(obj.get(this.keyFieldName));
                    result = JsonDocument.create(id, obj);
                    loadedDocuments++;
                }
            }
        }
        catch (IOException e) {
            result = null;
        }
        return result;
    }

    private int getRandomExpiry() {
        return this.expiryStart + random.nextInt(this.expiryEnd - this.expiryStart);
    }

    @Override
    public JsonDocument GetNextDocumentForUpdate() {
        JsonDocument doc = GetNextDocument();
        if (doc != null) {
            updater.updateJsonObject(doc.content());
        }
        return doc;
    }

    @Override
    public JsonDocument GetNextDocumentForDelete() {
        return GetNextDocument();
    }

    @Override
    public JsonDocument GetNextDocumentForInsert() {
        return GetNextDocument();
    }

    @Override
    public JsonDocument GetNextDocumentForTTL() {
        JsonDocument doc = GetNextDocument();
        if (doc != null) {
            int expiry = getRandomExpiry();
            return JsonDocument.create(doc.id(), expiry, doc.content());
        }
        else
            return doc;
    }

    @Override
    public String GetNextQuery() {
        String query = null;
        if (this.currentQueryIndex < this.queries.size()) {
            query = this.queries.get(this.currentQueryIndex++);
        }
        return query;
    }

    @Override
    public void close() {
        try {
            bufferedReader.close();
        }
        catch (IOException e) {
            System.err.println(e.toString());
        }
        return;
    }

    public BatchModeLoadData(DataInfo dataInfo, QueryInfo queryInfo, BatchModeTTLParameter ttlParam, BatchModeUpdateParameter updateParam) {
        super(dataInfo, queryInfo);
        this.ttlParam = ttlParam;
        this.updateParam = updateParam;
        File file = new File(dataInfo.dataFilePath);
        try {
            this.bufferedReader = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found " + dataInfo.dataFilePath);
        }
        this.keyFieldName = dataInfo.keyFieldName;
        this.maxDocumentsToLoad = dataInfo.docsToLoad;
        this.loadedDocuments = 0;
        this.expiryStart = ttlParam.expiryStart;
        this.expiryEnd = ttlParam.expiryEnd;
        this.updater = new JsonObjectUpdater(this.updateParam);
        this.currentQueryIndex = 0;
        for (String query : queryInfo.queries) {
            this.queries.add(query);
        }
    }
}
