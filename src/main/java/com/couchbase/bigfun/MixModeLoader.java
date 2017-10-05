package com.couchbase.bigfun;

import com.couchbase.client.java.document.JsonDocument;

import java.lang.*;
import java.io.*;
import java.util.Date;

public class MixModeLoader extends Loader<MixModeLoadParameter, MixModeLoadData> {

    private RandomCategoryGen operationRandomizer;

    private void sleepTillDueTime(Date dueTime)
    {
        Date currentDate = new Date();
        long msTillDueTime = dueTime.getTime() - currentDate.getTime();
        if (msTillDueTime > 0)
        {
            try {
                Thread.sleep(msTillDueTime);
            }
            catch (InterruptedException e) {
                System.err.println(e.toString());
                System.exit(-1);
            }
        }
    }

    @Override
    public void load() {
        Date currentOperationDueTime;
        if (this.getParameter().startTime.getTime() == 0 || this.getParameter().startTime.getTime() < (new Date()).getTime())
            currentOperationDueTime = new Date();
        else
            currentOperationDueTime = this.getParameter().startTime;

        sleepTillDueTime(currentOperationDueTime);

        Date operationEndTime = new Date(currentOperationDueTime.getTime() + this.getParameter().durationSeconds * 1000);

        while (true) {
            if ((new Date()).getTime() >= operationEndTime.getTime())
                break;

            if (this.getParameter().intervalMS != 0) {
                sleepTillDueTime(currentOperationDueTime);
                currentOperationDueTime = new Date(currentOperationDueTime.getTime() + this.getParameter().intervalMS);
            }

            try {
                String operation = operationRandomizer.nextCategory();
                operate(operation);
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                continue;
            }
        }
    }

    public MixModeLoader(MixModeLoadParameter loadParameter) {
        super(loadParameter, new MixModeLoadData(loadParameter.dataInfo, loadParameter.queryInfo,
                loadParameter.insertParameter, loadParameter.deleteParameter, loadParameter.ttlParameter));

        String operations[] = {
                INSERT_OPERATION,
                DELETE_OPERATION,
                UPDATE_OPERATION,
                TTL_OPERATION,
                QUERY_OPERATION};
        int operationPropotions[] = {
                this.getParameter().insertPropotion,
                this.getParameter().deletePropotion,
                this.getParameter().updatePropotion,
                this.getParameter().ttlPropotion,
                this.getParameter().queryPropotion};
        this.operationRandomizer = new RandomCategoryGen(operations, operationPropotions);
    }
}
