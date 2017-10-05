package com.couchbase.bigfun;

import java.util.Date;

public class MixModeLoadParameter extends LoadParameter {
    public int intervalMS;
    public long durationSeconds;
    public Date startTime;
    public int insertPropotion;
    public int updatePropotion;
    public int deletePropotion;
    public int ttlPropotion;
    public int queryPropotion;
    public MixModeInsertParameter insertParameter;
    public MixModeDeleteParameter deleteParameter;
    public MixModeTTLParameter ttlParameter;
    public MixModeLoadParameter(int intervalMS, int durationSeconds, Date startTime,
                                int insertPropotion, int updatePropotion,
                                int deletePropotion, int ttlPropotion, int queryPropotion,
                                DataInfo dataInfo, TargetInfo targetInfo, QueryInfo queryInfo,
                                MixModeInsertParameter insertParameter, MixModeDeleteParameter deleteParameter,
                                MixModeTTLParameter ttlParameter)
    {
        super(dataInfo, targetInfo, queryInfo);
        this.intervalMS = intervalMS;
        this.durationSeconds = durationSeconds;
        this.startTime = startTime;
        this.insertPropotion = insertPropotion;
        this.updatePropotion = updatePropotion;
        this.deletePropotion = deletePropotion;
        this.ttlPropotion = ttlPropotion;
        this.queryPropotion = queryPropotion;
        this.insertParameter = insertParameter;
        this.deleteParameter = deleteParameter;
        this.ttlParameter = ttlParameter;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MixModeLoadParameter)) {
            return false;
        }
        MixModeLoadParameter c = (MixModeLoadParameter) o;
        return super.equals(o) &&
                (intervalMS == c.intervalMS) &&
                (durationSeconds == c.durationSeconds) &&
                startTime.equals(c.startTime) &&
                (insertPropotion == c.insertPropotion) &&
                (updatePropotion == c.updatePropotion) &&
                (deletePropotion == c.deletePropotion) &&
                (ttlPropotion == c.ttlPropotion) &&
                (queryPropotion == c.queryPropotion) &&
                insertParameter.equals(c.insertParameter) &&
                deleteParameter.equals(c.deleteParameter) &&
                ttlParameter.equals(c.ttlParameter);
    }
}
