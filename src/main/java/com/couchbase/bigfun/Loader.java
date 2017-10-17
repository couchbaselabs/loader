package com.couchbase.bigfun;

import com.couchbase.client.java.document.JsonDocument;

import java.lang.*;
import java.util.Date;

public class Loader<PARAMT, DATAT> extends Thread {

        public static final String INSERT_OPERATION = "insert";
        public static final String DELETE_OPERATION = "delete";
        public static final String UPDATE_OPERATION = "update";
        public static final String TTL_OPERATION = "ttl";
        public static final String QUERY_OPERATION = "query";

        private LoadData data;

        private LoadParameter parameter;

        private LoadTarget target;

        private ResultFile resultFile;

        public LoadStats successStats;

        public LoadStats failedStats;

        public long duration;

        protected DATAT getData() {
            return (DATAT)data;
        }

        protected PARAMT getParameter() {
            return (PARAMT)parameter;
        }

        protected void printResult(String result) { resultFile.printResult(result); }

        /*
        To be override
         */
        protected void load() {
            while (true) {
                try {
                    if (!operate(INSERT_OPERATION))
                        break;
                }
                catch (Exception e) {
                    System.err.println(e.toString());
                    continue;
                }
            }
            return;
        }

        public void run() {
            Date start = new Date();
            load();
            Date end = new Date();
            this.duration = end.getTime() - start.getTime();
            this.target.close();
            this.data.close();
        }

        protected boolean operate(String operation) {
            boolean result;
            switch (operation) {
                case INSERT_OPERATION:
                    result = this.insert();
                    break;
                case DELETE_OPERATION:
                    result = this.delete();
                    break;
                case UPDATE_OPERATION:
                    result = this.update();
                    break;
                case TTL_OPERATION:
                    result = this.ttl();
                    break;
                case QUERY_OPERATION:
                    result = this.query();
                    break;
                default:
                    result = false;
                    break;
            }
            return result;
        }

        private boolean query() {
            boolean result;
            Date start = new Date();
            String query = "";
            try {
                query = data.GetNextQuery();
                if (query != null) {
                    LoadTarget.CBASQueryResult queryResult = this.target.cbasQuery(query, 3);
                    Date end = new Date();
                    this.successStats.queryNumber++;
                    this.successStats.queryLatency += end.getTime() - start.getTime();
                    this.printResult(String.format("CBAS success query:\t%s\tlatency:\t%d\tresult count:\t%d\telapseTime\t%s\texecutionTime:\t%s", query, end.getTime() - start.getTime(), queryResult.metrics.resultCount, queryResult.metrics.elapseTime, queryResult.metrics.executionTime));
                    result = true;
                }
                else
                    result = false;
            }
            catch (Exception e) {
                Date end = new Date();
                this.failedStats.queryNumber++;
                this.failedStats.queryLatency += end.getTime() - start.getTime();
                this.printResult(String.format("CBAS failed query:\t%s\tlatency:\t%d", query, end.getTime() - start.getTime()));
                throw e;
            }
            return result;
        }

        private boolean ttl() {
            boolean result;
            Date start = new Date();
            try {
                JsonDocument doc = data.GetNextDocumentForTTL();
                if (doc != null) {
                    this.target.upsert(doc);
                    Date end = new Date();
                    this.successStats.ttlNumber++;
                    this.successStats.ttlLatency += end.getTime() - start.getTime();
                    result = true;
                } else
                    result = false;
            } catch (Exception e) {
                Date end = new Date();
                this.failedStats.ttlNumber++;
                this.failedStats.ttlLatency += end.getTime() - start.getTime();
                throw e;
            }
            return result;
        }

        private boolean insert() {
            boolean result;
            Date start = new Date();
            try {
                JsonDocument doc = data.GetNextDocumentForInsert();
                if (doc != null) {
                    this.target.upsert(doc);
                    Date end = new Date();
                    this.successStats.insertNumber++;
                    this.successStats.insertLatency += end.getTime() - start.getTime();
                    result = true;
                }
                else
                    result = false;
            }
            catch (Exception e) {
                Date end = new Date();
                this.failedStats.insertNumber++;
                this.failedStats.insertLatency += end.getTime() - start.getTime();
                throw e;
            }
            return result;
        }

        private boolean delete() {
            boolean result;
            Date start = new Date();
            try {
                JsonDocument doc = data.GetNextDocumentForDelete();
                if (doc != null) {
                    this.target.delete(doc);
                    Date end = new Date();
                    this.successStats.deleteNumber++;
                    this.successStats.deleteLatency += end.getTime() - start.getTime();
                    result = true;
                }
                else
                    result = false;
            }
            catch (Exception e) {
                Date end = new Date();
                this.failedStats.deleteNumber++;
                this.failedStats.deleteLatency += end.getTime() - start.getTime();
                throw e;
            }
            return result;
        }

        private boolean update() {
            boolean result;
            Date start = new Date();
            try {
                JsonDocument doc = data.GetNextDocumentForUpdate();
                if (doc != null) {
                    this.target.upsert(doc);
                    Date end = new Date();
                    this.successStats.updateNumber++;
                    this.successStats.updateLatency += end.getTime() - start.getTime();
                    result = true;
                }
                else
                    result = false;
            }
            catch (Exception e) {
                Date end = new Date();
                this.failedStats.updateNumber++;
                this.failedStats.updateLatency += end.getTime() - start.getTime();
                throw e;
            }
            return result;
        }

        protected Loader(LoadParameter parameter, LoadData data, LoadTarget loadTarget, ResultFile resultFile) {
            super();
            this.parameter = parameter;
            this.data = data;
            if (loadTarget == null)
                this.target = new LoadTarget(this.parameter.targetInfo);
            else
                this.target = loadTarget;
            this.resultFile = resultFile;
            this.successStats = new LoadStats();
            this.failedStats = new LoadStats();
        }

        public Loader(LoadParameter parameter, LoadData data, ResultFile resultFile) {
            this(parameter, data, null, resultFile);
        }
    }

