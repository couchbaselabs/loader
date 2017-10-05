package com.couchbase.bigfun;

import java.util.ArrayList;

public class QueryInfo {
    public ArrayList<String> queries = new ArrayList<String>();
    public QueryInfo(){
        this(null);
    }
    public QueryInfo(ArrayList<String> queries){
        if (queries != null) {
            this.queries = queries;
        }
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof QueryInfo)) {
            return false;
        }
        QueryInfo c = (QueryInfo) o;
        return queries.equals(c.queries);
    }
}
