package com.couchbase.bigfun;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.TemporaryFailureException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.gson.Gson;

import java.util.Date;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeoutException;

/**
 * Unit test for simple App.
 */
public class LoaderTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public LoaderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( LoaderTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testGson() {
        Book book = new Book("a", "b1", "b2", "title", 100);
        Gson gson = new Gson();
        String bookjson = gson.toJson(book);
        Book book2 = gson.fromJson(bookjson, Book.class);
        boolean r = book.equals(book2);
        assertTrue( r );
    }

    private void testOneModeLoadParameter(MixModeLoadParameter p, Date date) {
        assertTrue(p.intervalMS == 6);
        assertTrue(p.durationSeconds == 7);
        assertTrue(p.startTime.equals(date));
        assertTrue(p.insertPropotion == 8);
        assertTrue(p.updatePropotion == 9);
        assertTrue(p.deletePropotion == 10);
        assertTrue(p.ttlPropotion == 11);
        assertTrue(p.queryPropotion == 12);
        assertTrue(p.dataInfo.dataFilePath.equals("datafilepath"));
        assertTrue(p.dataInfo.metaFilePath.equals("metafilepath"));
        assertTrue(p.dataInfo.keyFieldName.equals("keyfieldname"));
        assertTrue(p.dataInfo.docsToLoad == 1);
        assertTrue(p.targetInfo.host.equals("host"));
        assertTrue(p.targetInfo.bucket.equals("bucket"));
        assertTrue(p.targetInfo.username.equals("username"));
        assertTrue(p.targetInfo.password.equals("password"));
        assertTrue(p.targetInfo.cbashost.equals("cbashost"));
        assertTrue(p.queryInfo.queries.size() == 2);
        assertTrue(p.queryInfo.queries.get(0).equals("select 1;"));
        assertTrue(p.queryInfo.queries.get(1).equals("select 2;"));
        assertTrue(p.insertParameter.insertIdStart == 2);
        assertTrue(p.deleteParameter.maxDeleteIds == 3);
        assertTrue(p.ttlParameter.expiryStart == 4);
        assertTrue(p.ttlParameter.expiryEnd == 5);
    }

    public void testMixModeLoadParameters() {
        DataInfo dataInfo = new DataInfo("datafilepath", "metafilepath",  "keyfieldname", 1);
        TargetInfo targetInfo = new TargetInfo("host", "bucket", "username", "password", "cbashost");
        MixModeInsertParameter insertParameter = new MixModeInsertParameter(2);
        MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(3);
        MixModeTTLParameter ttlParameter = new MixModeTTLParameter(4, 5);
        Date date = new Date((new Date()).getTime() / 1000 * 1000);
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select 2;");
        MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(6, 7, date,
                8, 9, 10, 11, 12,
                dataInfo, targetInfo, queryInfo, insertParameter, deleteParameter, ttlParameter);
        MixModeLoadParameters mixModeLoadParameters = new MixModeLoadParameters();
        mixModeLoadParameters.loadParameters.add(mixModeLoadParameter);
        mixModeLoadParameters.loadParameters.add(mixModeLoadParameter);
        Gson gson = new Gson();
        String loadParamsString = gson.toJson(mixModeLoadParameters);
        MixModeLoadParameters mixModeLoadParameters2 = gson.fromJson(loadParamsString, MixModeLoadParameters.class);
        assertTrue(mixModeLoadParameters.loadParameters.size() == 2);
        MixModeLoadParameter mixModeLoadParameter2 = mixModeLoadParameters2.loadParameters.get(0);
        testOneModeLoadParameter(mixModeLoadParameter2, date);
        MixModeLoadParameter mixModeLoadParameter3 = mixModeLoadParameters2.loadParameters.get(1);
        testOneModeLoadParameter(mixModeLoadParameter3, date);
        assertTrue(mixModeLoadParameters.equals(mixModeLoadParameters2));
    }

    private void testOneBatchModeLoadParameter(BatchModeLoadParameter batchModeLoadParameter, String updateoperation, String updateFieldName,
                                               String updatefieldtype, String updateValueStart, String updateValueEnd,
                                               String updateValueFormat, String updateValuesFilePath) {
        assertTrue(batchModeLoadParameter.operation.equals(updateoperation));
        assertTrue(batchModeLoadParameter.ttlParameter.expiryStart == 5);
        assertTrue(batchModeLoadParameter.ttlParameter.expiryEnd == 6);
        assertTrue(batchModeLoadParameter.updateParameter.updateFieldName.equals(updateFieldName));
        assertTrue(batchModeLoadParameter.updateParameter.updateFieldType.equals(updatefieldtype));
        assertTrue(batchModeLoadParameter.updateParameter.updateValueStart.equals(updateValueStart));
        assertTrue(batchModeLoadParameter.updateParameter.updateValueEnd.equals(updateValueEnd));
        assertTrue(batchModeLoadParameter.updateParameter.updateValueFormat.equals(updateValueFormat));
        assertTrue(batchModeLoadParameter.updateParameter.updateValuesFilePath.equals(updateValuesFilePath));
        assertTrue(batchModeLoadParameter.dataInfo.dataFilePath.equals("datafilepath"));
        assertTrue(batchModeLoadParameter.dataInfo.metaFilePath.equals("metafilepath"));
        assertTrue(batchModeLoadParameter.dataInfo.keyFieldName.equals("keyfieldname"));
        assertTrue(batchModeLoadParameter.dataInfo.docsToLoad == 1);
        assertTrue(batchModeLoadParameter.targetInfo.host.equals("host"));
        assertTrue(batchModeLoadParameter.targetInfo.bucket.equals("bucket"));
        assertTrue(batchModeLoadParameter.targetInfo.username.equals("username"));
        assertTrue(batchModeLoadParameter.targetInfo.password.equals("password"));
        assertTrue(batchModeLoadParameter.targetInfo.cbashost.equals("cbashost"));
        assertTrue(batchModeLoadParameter.queryInfo.queries.size() == 2);
        assertTrue(batchModeLoadParameter.queryInfo.queries.get(0).equals("select 1;"));
        assertTrue(batchModeLoadParameter.queryInfo.queries.get(1).equals("select 2;"));
    }

    public void testBatchModeLoadParameters() {
        DataInfo dataInfo = new DataInfo("datafilepath", "metafilepath",  "keyfieldname", 1);
        TargetInfo targetInfo = new TargetInfo("host", "bucket", "username", "password", "cbashost");
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select 2;");
        Date date = new Date((new Date()).getTime() / 1000 * 1000);
        BatchModeUpdateParameter updateParameter1 = new BatchModeUpdateParameter("updatefieldname", "integer",
                "1", "2");
        BatchModeUpdateParameter updateParameter2 = new BatchModeUpdateParameter("updatefieldname", "string",
                "updatevaluesfilepath");
        BatchModeUpdateParameter updateParameter3 = new BatchModeUpdateParameter("updatefieldname", "string",
                "updatevalueformat", "3", "4");
        BatchModeTTLParameter ttlParameter = new BatchModeTTLParameter(5, 6);
        BatchModeLoadParameter batchModeLoadParameter1 = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", updateParameter1, ttlParameter);
        BatchModeLoadParameter batchModeLoadParameter2 = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"update", updateParameter2, ttlParameter);
        BatchModeLoadParameter batchModeLoadParameter3 = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"delete", updateParameter3, ttlParameter);
        BatchModeLoadParameters batchModeLoadParameters = new BatchModeLoadParameters();
        batchModeLoadParameters.loadParameters.add(batchModeLoadParameter1);
        batchModeLoadParameters.loadParameters.add(batchModeLoadParameter2);
        batchModeLoadParameters.loadParameters.add(batchModeLoadParameter3);
        Gson gson = new Gson();
        String loadParamsString = gson.toJson(batchModeLoadParameters);
        BatchModeLoadParameters batchModeLoadParameters2 = gson.fromJson(loadParamsString, BatchModeLoadParameters.class);
        assertTrue(batchModeLoadParameters2.loadParameters.size() == 3);
        BatchModeLoadParameter batchModeLoadParameter4 = batchModeLoadParameters2.loadParameters.get(0);
        testOneBatchModeLoadParameter(batchModeLoadParameter4, "insert", "updatefieldname", "integer",
                "1", "2", "", "");
        BatchModeLoadParameter batchModeLoadParameter5 = batchModeLoadParameters2.loadParameters.get(1);
        testOneBatchModeLoadParameter(batchModeLoadParameter5, "update", "updatefieldname", "string",
                "", "", "", "updatevaluesfilepath");
        BatchModeLoadParameter batchModeLoadParameter6 = batchModeLoadParameters2.loadParameters.get(2);
        testOneBatchModeLoadParameter(batchModeLoadParameter6, "delete", "updatefieldname", "string",
                "3", "4", "updatevalueformat", "");
        assertTrue(batchModeLoadParameters.equals(batchModeLoadParameters2));
    }

    public void testRandomCategoryGen()
    {
        {
            String categories[] = {"a", "b"};
            int propotions[] = {1000, 0};
            RandomCategoryGen randomCategoryGen = new RandomCategoryGen(categories, propotions);
            for (int i = 0; i < 100; i++) {
                assertTrue(randomCategoryGen.nextCategory().equals("a"));
            }
        }
        {
            String categories[] = {"a", "b"};
            int propotions[] = {0, 10};
            RandomCategoryGen randomCategoryGen = new RandomCategoryGen(categories, propotions);
            for (int i = 0; i < 100; i++) {
                assertTrue(randomCategoryGen.nextCategory().equals("b"));
            }
        }
        {
            String categories[] = {"a", "b", "c"};
            int propotions[] = {30, 30, 30};
            RandomCategoryGen randomCategoryGen = new RandomCategoryGen(categories, propotions);
            int ca = 0;
            int cb = 0;
            int cc = 0;
            int checknumber = 9000;
            for (int i = 0; i < checknumber; i++) {
                String category = randomCategoryGen.nextCategory();
                if (category.equals("a"))
                    ca++;
                else if (category.equals("b"))
                    cb++;
                else if (category.equals("c"))
                    cc++;
                else
                    assertTrue(false);
            }
            double pca = ((double)ca / (checknumber / 3));
            double pcb = ((double)cb / (checknumber / 3));
            double pcc = ((double)cc / (checknumber / 3));
            assertTrue(pca > 0.9 && pca < 1.1);
            assertTrue(pcb > 0.9 && pcb < 1.1);
            assertTrue(pcc > 0.9 && pcc < 1.1);
        }
    }

    private void createFileWithContents(String filename, String contents[])
    {
        try {
            File file = new File(filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < contents.length; i++) {
                writer.write(contents[i]);
                writer.write("\n");
            }
            writer.close();
        }
        catch (IOException e)
        {
            assertTrue(false);
        }
    }

    BatchModeUpdateParameter[] initializeUpdateParameters() {
        BatchModeUpdateParameter batchModeUpdateParameter[] = new BatchModeUpdateParameter[6];
        batchModeUpdateParameter[0] = new BatchModeUpdateParameter("intfield", "integer",
                "100000000001", "100000000005");
        batchModeUpdateParameter[1] = new BatchModeUpdateParameter("floatfield", "float",
                "100000000001.0", "100000000005.0");
        batchModeUpdateParameter[2] = new BatchModeUpdateParameter("datefield", "date",
                "2015-01-02", "2015-02-02");
        batchModeUpdateParameter[3] = new BatchModeUpdateParameter("timefield", "time",
                "2015-01-02T00:00:00", "2015-02-02T00:00:00");
        String updatevaluefilepath = "updatevaluesfilepath.txt";
        String contents[] = {"teststr1", "teststr2"};
        createFileWithContents(updatevaluefilepath, contents);
        batchModeUpdateParameter[4] = new BatchModeUpdateParameter("strsfield", "string",
                updatevaluefilepath);
        batchModeUpdateParameter[5] = new BatchModeUpdateParameter("strfmtfield", "string",
                "test%012d", "100000000001", "100000000005");
        return batchModeUpdateParameter;
    }

    public void testJsonObjectUpdater() {
        BatchModeUpdateParameter[] batchModeUpdateParameters = initializeUpdateParameters();
        int testnumber = 1000;
        for (int i = 0; i < testnumber; i ++) {
            JsonObjectUpdater ou1 = new JsonObjectUpdater(batchModeUpdateParameters[0]);
            JsonObject obj = JsonObject.fromJson("{\"intfield\" : 10, \"field2\" : \"abcd\"}");
            ou1.updateJsonObject(obj);
            Object v = obj.get("intfield");
            assertTrue(v instanceof Long);
            assertTrue((long)v >= 100000000001l && (long) v < 100000000005l);
        }
        for (int i = 0; i < testnumber; i ++) {
            JsonObjectUpdater ou1 = new JsonObjectUpdater(batchModeUpdateParameters[1]);
            JsonObject obj = JsonObject.fromJson("{\"floatfield\" : 10.0, \"field2\" : \"abcd\"}");
            ou1.updateJsonObject(obj);
            Object v = obj.get("floatfield");
            assertTrue(v instanceof Double);
            assertTrue((double)v >= 100000000001.0 && (double) v < 100000000005.0);
        }
        for (int i = 0; i < testnumber; i ++) {
            JsonObjectUpdater ou1 = new JsonObjectUpdater(batchModeUpdateParameters[2]);
            JsonObject obj = JsonObject.fromJson("{\"datefield\" : \"2000-01-02\", \"field2\" : \"abcd\"}");
            ou1.updateJsonObject(obj);
            Object v = obj.get("datefield");
            assertTrue(v instanceof String);
            assertTrue(((String)v).compareTo("2015-01-02") >= 0 && ((String) v).compareTo("2015-02-02") < 0);
        }
        for (int i = 0; i < testnumber; i ++) {
            JsonObjectUpdater ou1 = new JsonObjectUpdater(batchModeUpdateParameters[3]);
            JsonObject obj = JsonObject.fromJson("{\"timefield\" : \"2000-01-02T00:01:02\", \"field2\" : \"abcd\"}");
            ou1.updateJsonObject(obj);
            Object v = obj.get("timefield");
            assertTrue(v instanceof String);
            assertTrue(((String)v).compareTo("2015-01-02T00:00:00") >= 0 && ((String) v).compareTo("2015-02-02T00:00:00") < 0);
        }
        for (int i = 0; i < testnumber; i ++) {
            JsonObjectUpdater ou1 = new JsonObjectUpdater(batchModeUpdateParameters[4]);
            JsonObject obj = JsonObject.fromJson("{\"strsfield\" : \"test0\", \"field2\" : \"abcd\"}");
            ou1.updateJsonObject(obj);
            Object v = obj.get("strsfield");
            assertTrue(v instanceof String);
            assertTrue(((String)v).equals("teststr1") || ((String) v).equals("teststr2"));
        }
        for (int i = 0; i < testnumber; i ++) {
            JsonObjectUpdater ou1 = new JsonObjectUpdater(batchModeUpdateParameters[5]);
            JsonObject obj = JsonObject.fromJson("{\"strfmtfield\" : \"test0\", \"field2\" : \"abcd\"}");
            ou1.updateJsonObject(obj);
            Object v = obj.get("strfmtfield");
            assertTrue(v instanceof String);
            assertTrue(((String)v).compareTo("test100000000001") >= 0 && ((String) v).compareTo("test100000000005") < 0);
        }
    }

    private class LoadTargetTestTimeout extends LoadTarget
    {
        public int retryMax = 2;
        public int currentRetryCount = 0;

        @Override
        protected void upsertWithoutRetry(JsonDocument doc) {
            if (currentRetryCount > retryMax) {
                return;
            }
            else {
                this.currentRetryCount++;
                throw new RuntimeException("test timeout", new TimeoutException("test timeout"));
            }
        }

        @Override
        protected void deleteWithoutRetry(JsonDocument doc) {
            if (currentRetryCount > retryMax) {
                return;
            }
            else {
                this.currentRetryCount++;
                throw new RuntimeException("test timeout");
            }
        }

        public LoadTargetTestTimeout(TargetInfo targetInfo) {
            super(targetInfo);
            this.timeout = this.timeout / 2;
        }
    }

    // Env specific test, disabled by default remove "_" in method to enable
    public void _testLoadTarget()
    {
        TargetInfo targetinfo = new TargetInfo("172.23.98.29", "bucket-1", "bucket-1", "password", "172.23.98.30");
        LoadTarget target = new LoadTarget(targetinfo);
        String key = "1";
        String docJson = "{\"id\" : \"1\", \"updatefieldname\" : \"2000-01-02\", \"field2\" : \"abcd\"}";
        JsonDocument doc = JsonDocument.create(key, JsonObject.fromJson(docJson));
        target.upsert(doc);
        target.delete(doc);
        LoadTarget.CBASQueryResult o = target.cbasQuery("select count(*) from GleambookMessages;");
        assertTrue(o != null);
        assertTrue(o.status.equals("success"));
        assertTrue(o.metrics.resultCount == 1);
        try {
            o = target.cbasQuery("select count(*) from GleambookMessage");
            assertTrue(false);
        }
        catch (RuntimeException e) {
            assertTrue(true);
        }
        target.close();

        // Target with invalid IP for couchbase and cbas
        targetinfo = new TargetInfo("172.23.98.70", "bucket-1", "bucket-1", "password", "172.23.98.70");
        try {
            target = new LoadTarget(targetinfo);
            assertTrue(false);
        }
        catch (Exception e)
        {
            assertTrue(true);
        }

        targetinfo = new TargetInfo("172.23.98.29", "bucket-1", "bucket-1", "password", "172.23.98.70");
        target = new LoadTarget(targetinfo);
        try {
            o = target.cbasQuery("select count(*) from GleambookMessages;");
            assertTrue(false);
        }
        catch (Exception e) {
            assertTrue(true);
        }
        target.close();
    }

    // Env specific test, disabled by default remove "_" in method to enable
    public void _testLoadTargetRetry() {
        TargetInfo targetinfo = new TargetInfo("172.23.98.29", "bucket-1", "bucket-1", "password", "172.23.98.30");
        String key = "1";
        String docJson = "{\"id\" : \"1\", \"updatefieldname\" : \"2000-01-02\", \"field2\" : \"abcd\"}";
        JsonDocument doc = JsonDocument.create(key, JsonObject.fromJson(docJson));
        {
            LoadTargetTestTimeout target = new LoadTargetTestTimeout(targetinfo);
            target.upsert(doc);
            assertTrue(target.currentRetryCount == 3);
            target.close();
        }
        {
            LoadTargetTestTimeout target = new LoadTargetTestTimeout(targetinfo);
            try {
                target.delete(doc);
                assertTrue(false);
            } catch (Exception e) {
                assertTrue(e instanceof RuntimeException);
                assertTrue(e.getCause() == null);
                assertTrue(target.currentRetryCount == 1);
            }
            target.close();
        }
    }

    private class LoadParameterTest extends LoadParameter {
        public int updateNum;
        public int deleteNum;
        public int insertNum;
        public int ttlNum;
        public LoadParameterTest(DataInfo dataInfo, TargetInfo targetInfo, QueryInfo queryInfo,
                                 int updateNum, int deleteNum,
                                 int insertNum, int ttlNum) {
            super(dataInfo, targetInfo, queryInfo);
            this.updateNum = updateNum;
            this.deleteNum = deleteNum;
            this.insertNum = insertNum;
            this.ttlNum = ttlNum;
        }
    }

    private class LoadDataTest extends LoadData {
        public LoadParameterTest loadParameter;
        public int updateCnt = 0;
        public int deleteCnt = 0;
        public int insertCnt = 0;
        public int ttlCnt = 0;
        public int queryCnt = 0;
        private final JsonDocument doc;
        @Override
        public JsonDocument GetNextDocumentForUpdate() {
            if (updateCnt >= loadParameter.updateNum)
                return null;
            else {
                updateCnt++;
                return doc;
            }
        }
        @Override
        public JsonDocument GetNextDocumentForDelete() {
            if (deleteCnt >= loadParameter.deleteNum)
                return null;
            else {
                deleteCnt++;
                return doc;
            }
        }
        @Override
        public JsonDocument GetNextDocumentForInsert() {
            if (insertCnt >= loadParameter.insertNum)
                return null;
            else {
                insertCnt++;
                return doc;
            }
        }
        @Override
        public JsonDocument GetNextDocumentForTTL() {
            if (ttlCnt >= loadParameter.ttlNum)
                return null;
            else {
                ttlCnt++;
                return doc;
            }
        }
        @Override
        public String GetNextQuery() {
            if (queryCnt >= loadParameter.queryInfo.queries.size())
                return null;
            else
                return loadParameter.queryInfo.queries.get(queryCnt++);
        }
        @Override
        public void close() {
            return;
        }
        public LoadDataTest(DataInfo dataInfo, QueryInfo queryInfo, LoadParameterTest loadParameter) {
            super(dataInfo, queryInfo);
            this.loadParameter = loadParameter;
            doc = JsonDocument.create("1", JsonObject.fromJson("{\"id\" : \"1\"}"));
        }
    }

    private class LoadTargetTest extends LoadTarget
    {
        public int upsertCnt = 0;
        public int deleteCnt = 0;
        public int queryCnt = 0;
        @Override
        protected void upsertWithoutRetry(JsonDocument doc) {
            upsertCnt++;
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {}
            if (upsertCnt == 1)
                throw new TemporaryFailureException("test loader");
            return;
        }

        @Override
        protected void deleteWithoutRetry(JsonDocument doc) {
            deleteCnt++;
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {}
            if (deleteCnt == 1)
                throw new RuntimeException("test loader");
            return;
        }

        @Override
        public CBASQueryResult cbasQuery(String query) {
            this.queryCnt++;
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {}
            if (queryCnt == 1)
                throw new RuntimeException("test loader");
            return new CBASQueryResult("success", new CBASQueryMetrics("a", "b", 10, 10));
        }

        public LoadTargetTest(TargetInfo targetInfo) {
            super(targetInfo);
            this.timeout = this.timeout / 2;
        }
    }

    private class LoaderTestClass extends Loader<LoadParameterTest, LoadDataTest> {
        public LoadDataTest getDataForTest() {return getData();}
        public LoadParameterTest getParameterForTest() {return getParameter();}
        @Override
        protected void load() {
            while (true) {
                try {
                    if (!operate("insert"))
                        break;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    continue;
                }
            }
            while (true) {
                try {
                    if (!operate("update"))
                        break;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    continue;
                }
            }
            while (true) {
                try {
                    if (!operate("delete"))
                        break;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    continue;
                }
            }
            while (true) {
                try {
                    if (!operate("ttl"))
                        break;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    continue;
                }
            }
            while (true) {
                try {
                    if (!operate("query"))
                        break;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    continue;
                }
            }
        }

        public LoaderTestClass(LoadParameterTest parameter, LoadTargetTest loadTargetTest) {
            super(parameter, new LoadDataTest(parameter.dataInfo, parameter.queryInfo, parameter), (LoadTarget)loadTargetTest);
        }
    }

    // Env specific test, disabled by default remove "_" in method to enable
    public void _testLoader() {
        int updateNum = 10;
        int deleteNum = 11;
        int insertNum = 12;
        int ttlNum = 13;
        DataInfo dataInfo = new DataInfo("data.json", "data.meta", "id", 10);
        TargetInfo targetInfo = new TargetInfo("172.23.98.29", "bucket-1", "bucket-1", "password", "172.23.98.30");
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select 2;");
        LoadParameterTest loadParam = new LoadParameterTest(dataInfo, targetInfo, queryInfo, updateNum, deleteNum, insertNum, ttlNum);
        LoadTargetTest loadTarget = new LoadTargetTest(targetInfo);
        LoaderTestClass loader = new LoaderTestClass(loadParam, loadTarget);
        loader.start();
        try {
            loader.join();
        }
        catch (Exception e) {
            assertTrue(false);
        }
        assertTrue(loader.getParameterForTest().updateNum == updateNum);
        assertTrue(loader.getParameterForTest().deleteNum == deleteNum);
        assertTrue(loader.getParameterForTest().insertNum == insertNum);
        assertTrue(loader.getParameterForTest().ttlNum == ttlNum);
        assertTrue(loader.getParameterForTest().queryInfo.queries.size() == queryInfo.queries.size());
        assertTrue(loader.getDataForTest().updateCnt == updateNum);
        assertTrue(loader.getDataForTest().insertCnt == insertNum);
        assertTrue(loader.getDataForTest().ttlCnt == ttlNum);
        assertTrue(loader.getDataForTest().deleteCnt == deleteNum);
        assertTrue(loader.getDataForTest().queryCnt == queryInfo.queries.size());
        assertTrue(loadTarget.deleteCnt == deleteNum);
        assertTrue(loadTarget.upsertCnt == (updateNum + insertNum + ttlNum + 1));

        assertTrue(loader.successStats.queryNumber == 1);
        assertTrue(loader.failedStats.queryNumber == 1);
        assertTrue(loader.successStats.queryLatency >= 10 && loader.successStats.queryLatency < 15);
        assertTrue(loader.failedStats.queryLatency >= 10 && loader.failedStats.queryLatency < 15);

        assertTrue(loader.successStats.deleteNumber == deleteNum - 1);
        assertTrue(loader.failedStats.deleteNumber == 1);
        assertTrue(loader.successStats.insertNumber + loader.successStats.updateNumber + loader.successStats.ttlNumber ==
                insertNum + updateNum + ttlNum);
        assertTrue(loader.failedStats.insertNumber + loader.failedStats.updateNumber + loader.failedStats.ttlNumber ==
                0);

        assertTrue(loader.duration >= loadTarget.timeout + 10 * (loadTarget.deleteCnt + loadTarget.upsertCnt) && loader.duration <= 1.5 * (loadTarget.timeout + 10 * (loadTarget.deleteCnt + loadTarget.upsertCnt)));
        long successUpsertLatency = loader.successStats.updateLatency + loader.successStats.insertLatency + loader.successStats.ttlLatency;
        long successUpsertNumber = loader.successStats.updateNumber + loader.successStats.insertNumber + loader.successStats.ttlNumber;
        long failedUpsertLatency = loader.failedStats.updateLatency + loader.failedStats.insertLatency + loader.failedStats.ttlLatency;

        assertTrue( successUpsertLatency >= loadTarget.timeout + 10 * successUpsertNumber && successUpsertLatency <= 1.5 * (loadTarget.timeout + 10 * successUpsertNumber));
        assertTrue(failedUpsertLatency == 0);
        assertTrue( loader.successStats.deleteLatency >= 10 * loader.successStats.deleteNumber && loader.successStats.deleteLatency <= 1.5 * (10 * loader.successStats.deleteNumber));
        assertTrue( loader.failedStats.deleteLatency >= 10 * loader.failedStats.deleteNumber && loader.failedStats.deleteLatency <= 1.5 * (10 * loader.failedStats.deleteNumber));
    }

    private String[] initializeJsonDocs(int size) {
        String docContents[] = new String[size];
        for (int i = 0; i < size; i++) {
            String content = String.format("{\"id\":%d, \"intfield\":1000000000000, \"floatfield\":1.1, \"datefield\":\"2000-01-01\", \"timefield\":\"2001-01-01T00:00:00\", \"strsfield\":\"str1\", \"strfmtfield\":\"strfmt001\"}", i + 1);
            docContents[i] = content;
        }
        return docContents;
    }

    // Env specific test, disabled by default remove "_" in method to enable
    public void _testBatchModeLoader() {
        String dataFile = "data.json";
        String metaFile = "data.meta";
        String docContents[] = initializeJsonDocs(20);
        DataInfo dataInfo = new DataInfo(dataFile, metaFile, "id", docContents.length);
        createFileWithContents(dataFile, docContents);

        String contentsMeta[] = {String.format("IDRange=1:%d", docContents.length)};
        createFileWithContents(metaFile, contentsMeta);

        TargetInfo targetInfo = new TargetInfo("172.23.98.29", "bucket-1", "bucket-1", "password", "172.23.98.30");

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select * from x;");

        BatchModeUpdateParameter batchModeUpdateParameters[] = initializeUpdateParameters();
        BatchModeTTLParameter batchModeTTLParameter = new BatchModeTTLParameter(1, 10);

        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", batchModeUpdateParameters[0], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == docContents.length);
            assertTrue(loader.successStats.deleteNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0);
            assertTrue(loader.successStats.queryNumber == 0);
            assertTrue(loader.failedStats.insertNumber == 0);
            assertTrue(loader.failedStats.deleteNumber == 0);
            assertTrue(loader.failedStats.updateNumber == 0);
            assertTrue(loader.failedStats.ttlNumber == 0);
            assertTrue(loader.failedStats.queryNumber == 0);
        }

        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"delete", batchModeUpdateParameters[0], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.deleteNumber == docContents.length);
            assertTrue(loader.successStats.updateNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0);
            assertTrue(loader.successStats.queryNumber == 0);
            assertTrue(loader.failedStats.insertNumber == 0);
            assertTrue(loader.failedStats.deleteNumber == 0);
            assertTrue(loader.failedStats.updateNumber == 0);
            assertTrue(loader.failedStats.ttlNumber == 0);
            assertTrue(loader.failedStats.queryNumber == 0);
        }

        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", batchModeUpdateParameters[0], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            } catch (InterruptedException e) {
                assertTrue(false);
            }
        }

        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"ttl", batchModeUpdateParameters[0], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
                Thread.sleep(10*1000);
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0);
            assertTrue(loader.successStats.ttlNumber == docContents.length);
            assertTrue(loader.successStats.queryNumber == 0);
            assertTrue(loader.failedStats.insertNumber == 0);
            assertTrue(loader.failedStats.deleteNumber == 0);
            assertTrue(loader.failedStats.updateNumber == 0);
            assertTrue(loader.failedStats.ttlNumber == 0);
            assertTrue(loader.failedStats.queryNumber == 0);
        }

        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"query", batchModeUpdateParameters[0], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0);
            assertTrue(loader.successStats.queryNumber == 1);
            assertTrue(loader.failedStats.insertNumber == 0);
            assertTrue(loader.failedStats.deleteNumber == 0);
            assertTrue(loader.failedStats.updateNumber == 0);
            assertTrue(loader.failedStats.ttlNumber == 0);
            assertTrue(loader.failedStats.queryNumber == 1);
        }

        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", batchModeUpdateParameters[0], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            } catch (InterruptedException e) {
                assertTrue(false);
            }
        }

        for (int i = 0; i < batchModeUpdateParameters.length; i++)
        {
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"update", batchModeUpdateParameters[i], batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0);
            assertTrue(loader.successStats.updateNumber == docContents.length);
            assertTrue(loader.successStats.ttlNumber == 0);
            assertTrue(loader.successStats.queryNumber == 0);
            assertTrue(loader.failedStats.insertNumber == 0);
            assertTrue(loader.failedStats.deleteNumber == 0);
            assertTrue(loader.failedStats.updateNumber == 0);
            assertTrue(loader.failedStats.ttlNumber == 0);
            assertTrue(loader.failedStats.queryNumber == 0);
        }
    }

    private class MixModeLoaderTest extends MixModeLoader {
        public MixModeLoadData getData() { return super.getData(); }
        public MixModeLoaderTest(MixModeLoadParameter loadParameter){
            super(loadParameter);
        }
    }

    // Env specific test, disabled by default remove "_" in method to enable
    public void _testMixModeLoader() {
        String dataFile = "data.json";
        String metaFile = "data.meta";
        String docContents[] = initializeJsonDocs(200);
        DataInfo dataInfo = new DataInfo(dataFile, metaFile, "id", docContents.length);
        createFileWithContents(dataFile, docContents);

        String contentsMeta[] = {String.format("IDRange=1:%d", docContents.length)};
        createFileWithContents(metaFile, contentsMeta);

        long extraIdStart = 10000;

        TargetInfo targetInfo = new TargetInfo("172.23.98.29", "bucket-1", "bucket-1", "password", "172.23.98.30");

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select 2;");

        {
            BatchModeUpdateParameter batchModeUpdateParameter = new BatchModeUpdateParameter("intfield", "integer", "1", "2");
            BatchModeTTLParameter batchModeTTLParameter = new BatchModeTTLParameter(1, 10);
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", batchModeUpdateParameter, batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber == docContents.length && loader.failedStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
        }

        {
            MixModeInsertParameter insertParameter = new MixModeInsertParameter(extraIdStart);
            MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(1);
            MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 2);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(50, 20, new Date(),
                    100, 0, 0, 0, 0,
                    dataInfo, targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            MixModeLoaderTest loader = new MixModeLoaderTest(mixModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e) {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber >= 399 && loader.successStats.insertNumber <= 401 && loader.failedStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
            assertTrue(loader.getData().getCurrentExtraInsertId() == loader.successStats.insertNumber + extraIdStart);
            assertTrue(loader.getData().getRemovedKeysNumber() == 0);
        }

        {
            MixModeInsertParameter insertParameter = new MixModeInsertParameter(extraIdStart);
            MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(1);
            MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 2);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(50, 20, new Date(),
                    0, 100, 0, 0, 0,
                    dataInfo, targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            MixModeLoader loader = new MixModeLoader(mixModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e) {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber == 0 && loader.failedStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber >= 399 && loader.successStats.updateNumber <= 401 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
            assertTrue(loader.getData().getCurrentExtraInsertId() == extraIdStart);
            assertTrue(loader.getData().getRemovedKeysNumber() == 0);
        }

        {
            MixModeInsertParameter insertParameter = new MixModeInsertParameter(extraIdStart);
            MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(10);
            MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 2);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(200, 10, new Date(),
                    0, 0, 100, 0, 0,
                    dataInfo, targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            MixModeLoader loader = new MixModeLoader(mixModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e) {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber == 0 && loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber >= 49 && loader.successStats.deleteNumber <= 51 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
            assertTrue(loader.getData().getCurrentExtraInsertId() == extraIdStart);
            assertTrue(loader.getData().getRemovedKeysNumber() == loader.successStats.deleteNumber);
        }

        {
            BatchModeUpdateParameter batchModeUpdateParameter = new BatchModeUpdateParameter("intfield", "integer", "1", "2");
            BatchModeTTLParameter batchModeTTLParameter = new BatchModeTTLParameter(1, 10);
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", batchModeUpdateParameter, batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber == docContents.length && loader.failedStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
        }

        {
            MixModeInsertParameter insertParameter = new MixModeInsertParameter(extraIdStart);
            MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(10);
            MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 2);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(100, 20, new Date(),
                    0, 0, 0, 100, 0,
                    dataInfo, targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            MixModeLoader loader = new MixModeLoader(mixModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e) {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber == 0 && loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.ttlNumber >= 99 && loader.successStats.ttlNumber <= 101 && loader.failedStats.ttlNumber >= 99 && loader.failedStats.ttlNumber <= 101);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.getData().getCurrentExtraInsertId() == extraIdStart);
            assertTrue(loader.getData().getRemovedKeysNumber() == loader.successStats.ttlNumber);
        }

        {
            MixModeInsertParameter insertParameter = new MixModeInsertParameter(extraIdStart);
            MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(10);
            MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 2);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(200, 10, new Date(),
                    0, 0, 0, 0, 100,
                    dataInfo, targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            MixModeLoader loader = new MixModeLoader(mixModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e) {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == 0 && loader.successStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
            assertTrue(loader.successStats.queryNumber >= 49 && loader.successStats.queryNumber < 52 && loader.failedStats.queryNumber == 0);
        }

        {
            BatchModeUpdateParameter batchModeUpdateParameter = new BatchModeUpdateParameter("intfield", "integer", "1", "2");
            BatchModeTTLParameter batchModeTTLParameter = new BatchModeTTLParameter(1, 10);
            BatchModeLoadParameter batchModeLoadParameter = new BatchModeLoadParameter(dataInfo, targetInfo, queryInfo,"insert", batchModeUpdateParameter, batchModeTTLParameter);
            BatchModeLoader loader = new BatchModeLoader(batchModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e)
            {
                assertTrue(false);
            }
            assertTrue(loader.successStats.insertNumber == docContents.length && loader.failedStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber == 0 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.deleteNumber == 0 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber == 0 && loader.failedStats.ttlNumber == 0);
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
        }

        {
            MixModeInsertParameter insertParameter = new MixModeInsertParameter(10000);
            MixModeDeleteParameter deleteParameter = new MixModeDeleteParameter(10);
            MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 2);
            MixModeLoadParameter mixModeLoadParameter = new MixModeLoadParameter(50, 30, new Date(),
                    100, 100, 50, 50, 0,
                    dataInfo, targetInfo, queryInfo,
                    insertParameter, deleteParameter, ttlParameter);
            MixModeLoader loader = new MixModeLoader(mixModeLoadParameter);
            loader.start();
            try {
                loader.join();
            }
            catch (InterruptedException e) {
                assertTrue(false);
            }
            assertTrue(loader.successStats.queryNumber == 0 && loader.failedStats.queryNumber == 0);
            assertTrue(loader.successStats.insertNumber >= 160 && loader.successStats.insertNumber <= 240 && loader.failedStats.insertNumber == 0);
            assertTrue(loader.successStats.updateNumber >= 160 && loader.successStats.updateNumber <= 240 && loader.failedStats.updateNumber == 0);
            assertTrue(loader.successStats.ttlNumber >= 80 && loader.successStats.ttlNumber <= 120 && loader.failedStats.ttlNumber == 0);
            assertTrue(loader.successStats.deleteNumber >= 80 && loader.successStats.deleteNumber <= 120 && loader.failedStats.deleteNumber == 0);
            assertTrue(loader.successStats.ttlNumber + loader.successStats.deleteNumber == loader.getData().getRemovedKeysNumber() +
                    loader.successStats.insertNumber - (loader.getData().getCurrentExtraInsertId() - extraIdStart));
        }
    }

    public void testBatchModeLoadData() {
        String dataFile = "data.json";
        String metaFile = "data.meta";
        String docContents[] = initializeJsonDocs(200);
        DataInfo dataInfo = new DataInfo(dataFile, metaFile, "id", docContents.length);
        createFileWithContents(dataFile, docContents);

        String contentsMeta[] = {String.format("IDRange=1:%d", docContents.length)};
        createFileWithContents(metaFile, contentsMeta);

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select 2;");

        BatchModeUpdateParameter batchModeUpdateParameter = new BatchModeUpdateParameter("intfield", "integer", "1000", "1005");
        BatchModeTTLParameter batchModeTTLParameter = new BatchModeTTLParameter(1, 10);

        BatchModeLoadData data = new BatchModeLoadData(dataInfo, queryInfo, batchModeTTLParameter, batchModeUpdateParameter);

        JsonDocument doc = null;
        for (long i = 0; i < docContents.length; i ++)
        {
            if (i % 4 == 0) {
                doc = data.GetNextDocumentForInsert();
                assertTrue((int)(doc.content().get("id")) == i+1);
            }
            else if (i % 4 == 1) {
                doc = data.GetNextDocumentForUpdate();
                assertTrue((long)doc.content().get("intfield") >= 1000 && (long)doc.content().get("intfield") < 1005 );
            }
            else if (i % 4 == 2) {
                doc = data.GetNextDocumentForDelete();
            }
            else if (i % 4 == 3) {
                doc = data.GetNextDocumentForTTL();
                assertTrue(doc.expiry() >= 1 && doc.expiry() < 10);
            }
            assertTrue(doc.id().equals(String.valueOf(i+1)));
        }
        assertTrue(data.GetNextQuery().equals("select 1;"));
        assertTrue(data.GetNextQuery().equals("select 2;"));
        assertTrue(data.GetNextQuery() == null);
        assertTrue(data.GetNextDocumentForDelete() == null);
        assertTrue(data.GetNextDocumentForInsert() == null);
        assertTrue(data.GetNextDocumentForUpdate() == null);
        assertTrue(data.GetNextDocumentForTTL() == null);
        data.close();
    }

    public void testMixModeLoadData() {
        String dataFile = "data.json";
        String metaFile = "data.meta";
        String docContents[] = initializeJsonDocs(200);
        DataInfo dataInfo = new DataInfo(dataFile, metaFile, "id", docContents.length);
        createFileWithContents(dataFile, docContents);

        String contentsMeta[] = {String.format("IDRange=1:%d", docContents.length)};
        createFileWithContents(metaFile, contentsMeta);

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.queries.add("select 1;");
        queryInfo.queries.add("select 2;");

        MixModeInsertParameter insertParam = new MixModeInsertParameter(1000000000);
        MixModeDeleteParameter delParam = new MixModeDeleteParameter(10);
        MixModeTTLParameter ttlParameter = new MixModeTTLParameter(1, 10);
        MixModeLoadData data = new MixModeLoadData(dataInfo, queryInfo, insertParam, delParam, ttlParameter);

        JsonDocument doc = null;
        doc = data.GetNextDocumentForInsert();
        assertTrue(doc.id().equals(String.valueOf(1000000000)));
        assertTrue(data.getCurrentExtraInsertId() == 1000000001);
        for (int i = 0; i < 9; i ++) {
            doc = data.GetNextDocumentForDelete();
            assertTrue(Long.valueOf(doc.id()) >= 1 && Long.valueOf(doc.id()) <= docContents.length);
            assertTrue(data.getRemovedKeysNumber() == i + 1);
        }

        doc = data.GetNextDocumentForInsert();
        assertTrue(doc.id().equals(String.valueOf(1000000001)));
        assertTrue(data.getCurrentExtraInsertId() == 1000000002);

        doc = data.GetNextDocumentForDelete();
        assertTrue(Long.valueOf(doc.id()) >= 1 && Long.valueOf(doc.id()) <= docContents.length);
        assertTrue(data.getRemovedKeysNumber() == 10);

        doc = data.GetNextDocumentForInsert();
        assertTrue(Long.valueOf(doc.id()) >= 1 && Long.valueOf(doc.id()) <= docContents.length);
        assertTrue(data.getCurrentExtraInsertId() == 1000000002);
        assertTrue(data.getRemovedKeysNumber() == 9);

        doc = data.GetNextDocumentForDelete();
        assertTrue(Long.valueOf(doc.id()) >= 1 && Long.valueOf(doc.id()) <= docContents.length);
        assertTrue(data.getRemovedKeysNumber() == 10);

        for (int i = 0; i < 45; i ++) {
            doc = data.GetNextDocumentForTTL();
            assertTrue(Long.valueOf(doc.id()) >= 1 && Long.valueOf(doc.id()) <= docContents.length);
            assertTrue(data.getRemovedKeysNumber() == 11 + i * 2);
            doc = data.GetNextDocumentForDelete();
            assertTrue(Long.valueOf(doc.id()) >= 1 && Long.valueOf(doc.id()) <= docContents.length);
            assertTrue(data.getRemovedKeysNumber() == 12 + i * 2);
        }

        try {
            doc = data.GetNextDocumentForTTL();
            assertTrue(false);
        }
        catch (RuntimeException e)
        { assertTrue(e.getMessage().equals("Too much documents removed"));}
        try {
            doc = data.GetNextDocumentForDelete();
            assertTrue(false);
        }
        catch (RuntimeException e)
        { assertTrue(e.getMessage().equals("Too much documents removed"));}

        assertTrue(data.getCurrentExtraInsertId() == 1000000002);
        assertTrue(data.getRemovedKeysNumber() == 100);
        for (int i = 0; i < 10; i++) {
            String query = data.GetNextQuery();
            assertTrue(query.equals("select 1;") || query.equals("select 2;"));
        }
        data.close();
    }

    public void testLoadStats() {
        LoadStats s1 = new LoadStats();
        s1.insertNumber = 1;
        s1.deleteNumber = 2;
        s1.updateNumber = 3;
        s1.ttlNumber = 4;
        s1.queryNumber = 9;
        s1.insertLatency = 5;
        s1.deleteLatency = 6;
        s1.updateLatency = 7;
        s1.ttlLatency = 8;
        s1.queryLatency = 10;

        LoadStats s2 = new LoadStats();
        s2.insertNumber = 1;
        s2.deleteNumber = 2;
        s2.updateNumber = 3;
        s2.ttlNumber = 4;
        s2.queryNumber = 9;
        s2.insertLatency = 5;
        s2.deleteLatency = 6;
        s2.updateLatency = 7;
        s2.ttlLatency = 8;
        s2.queryLatency = 10;

        s1.add(s2);
        assertTrue(s1.insertNumber == 2 * s2.insertNumber);
        assertTrue(s1.deleteNumber == 2 * s2.deleteNumber);
        assertTrue(s1.updateNumber == 2 * s2.updateNumber);
        assertTrue(s1.ttlNumber == 2 * s2.ttlNumber);
        assertTrue(s1.queryNumber == 2* s2.queryNumber);
        assertTrue(s1.insertLatency == 2 * s2.insertLatency);
        assertTrue(s1.deleteLatency == 2 * s2.deleteLatency);
        assertTrue(s1.updateLatency == 2 * s2.updateLatency);
        assertTrue(s1.ttlLatency == 2 * s2.ttlLatency);
        assertTrue(s1.queryLatency == 2 * s2.queryLatency);
        String s = s1.toString();
    }
}
