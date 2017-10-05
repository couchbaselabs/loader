package com.couchbase.bigfun;

public class LoadParameter {
    public DataInfo dataInfo;
    public TargetInfo targetInfo;
    public QueryInfo queryInfo;
    public LoadParameter(DataInfo dataInfo, TargetInfo targetInfo) {
        this(dataInfo, targetInfo, null);
    }
    public LoadParameter(DataInfo dataInfo, TargetInfo targetInfo, QueryInfo queryInfo) {
        this.dataInfo = dataInfo;
        this.targetInfo = targetInfo;
        if (queryInfo == null)
            this.queryInfo = new QueryInfo();
        else
            this.queryInfo = queryInfo;
    }
    @Override
    public boolean equals(Object o){
        if (o == this) {
            return true;
        }
        if (!(o instanceof LoadParameter)) {
            return false;
        }
        LoadParameter c = (LoadParameter) o;
        return dataInfo.equals(c.dataInfo) &&
                targetInfo.equals(c.targetInfo) &&
                queryInfo.equals(c.queryInfo);
    }
}
