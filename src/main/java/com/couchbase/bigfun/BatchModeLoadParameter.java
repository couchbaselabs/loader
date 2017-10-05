package com.couchbase.bigfun;

public class BatchModeLoadParameter extends LoadParameter {
    public String operation;
    public BatchModeUpdateParameter updateParameter;
    public BatchModeTTLParameter ttlParameter;
    public BatchModeLoadParameter(DataInfo dataInfo, TargetInfo targetInfo, QueryInfo queryInfo, String operation,
                                  BatchModeUpdateParameter updateParameter, BatchModeTTLParameter ttlParameter) {
        super(dataInfo, targetInfo, queryInfo);
        this.operation = operation;
        this.updateParameter = updateParameter;
        this.ttlParameter = ttlParameter;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BatchModeLoadParameter)) {
            return false;
        }
        BatchModeLoadParameter c = (BatchModeLoadParameter) o;
        return super.equals(o) &&
                operation.equals(c.operation) &&
                updateParameter.equals(c.updateParameter) &&
                ttlParameter.equals(c.ttlParameter);
    }

}
