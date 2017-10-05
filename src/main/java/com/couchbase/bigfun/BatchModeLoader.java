package com.couchbase.bigfun;

import java.lang.Thread;
import java.util.Date;

public class BatchModeLoader extends Loader<BatchModeLoadParameter, BatchModeLoadData> {

    private String operation;

    @Override
    public void load() {
        while (true) {
            try {
                if (!operate(this.operation))
                    break;
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                continue;
            }
        }
    }

    public BatchModeLoader(BatchModeLoadParameter loadParameter) {
        super(loadParameter, new BatchModeLoadData(loadParameter.dataInfo, loadParameter.queryInfo,
                loadParameter.ttlParameter, loadParameter.updateParameter));
        this.operation = loadParameter.operation;
    }
}
