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

        public LoadStats successStats;

        public LoadStats failedStats;

        public long duration;

        protected DATAT getData() {
            return (DATAT)data;
        }

        protected PARAMT getParameter() {
            return (PARAMT)parameter;
        }

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
            try {
                String query = data.GetNextQuery();
                if (query != null) {
                    this.target.cbasQuery(query);
                    Date end = new Date();
                    this.successStats.queryNumber++;
                    this.successStats.queryLatency += end.getTime() - start.getTime();
                    result = true;
                }
                else
                    result = false;
            }
            catch (Exception e) {
                Date end = new Date();
                this.failedStats.queryNumber++;
                this.failedStats.queryLatency += end.getTime() - start.getTime();
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

        protected Loader(LoadParameter parameter, LoadData data, LoadTarget loadTarget) {
            super();
            this.parameter = parameter;
            this.data = data;
            if (loadTarget == null)
                this.target = new LoadTarget(this.parameter.targetInfo);
            else
                this.target = loadTarget;
            this.successStats = new LoadStats();
            this.failedStats = new LoadStats();
        }

        public Loader(LoadParameter parameter, LoadData data) {
            this(parameter, data, null);
        }
    }

