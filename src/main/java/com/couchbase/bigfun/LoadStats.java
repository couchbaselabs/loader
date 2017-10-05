package com.couchbase.bigfun;

public class LoadStats {
    public long insertNumber = 0;
    public long deleteNumber = 0;
    public long updateNumber = 0;
    public long ttlNumber = 0;
    public long queryNumber = 0;
    public long insertLatency = 0;
    public long deleteLatency = 0;
    public long updateLatency = 0;
    public long ttlLatency = 0;
    public long queryLatency = 0;
    public void add(LoadStats other) {
        this.insertNumber += other.insertNumber;
        this.deleteNumber += other.deleteNumber;
        this.updateNumber += other.updateNumber;
        this.ttlNumber += other.ttlNumber;
        this.queryNumber += other.queryNumber;
        this.insertLatency += other.insertLatency;
        this.deleteLatency += other.deleteLatency;
        this.updateLatency += other.updateLatency;
        this.ttlLatency += other.ttlLatency;
        this.queryLatency += other.queryLatency;
    }
    @Override
    public String toString() {
        return String.format("insertNumber=%d\n", this.insertNumber) +
                String.format("deleteNumber=%d\n", this.deleteNumber) +
                String.format("updateNumber=%d\n", this.updateNumber) +
                String.format("ttlNumber=%d\n", this.ttlNumber) +
                String.format("queryNumber=%d\n", this.queryNumber) +
                String.format("insertLatency=%d\n", this.insertLatency) +
                String.format("deleteLatency=%d\n", this.deleteLatency) +
                String.format("updateLatency=%d\n", this.updateLatency) +
                String.format("ttlLatency=%d", this.ttlLatency) +
                String.format("queryLatency=%d", this.queryLatency);
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LoadStats)) {
            return false;
        }
        LoadStats c = (LoadStats) o;
        return (insertNumber == c.insertNumber) &&
                (deleteNumber == c.deleteNumber) &&
                (updateNumber == c.updateNumber) &&
                (ttlNumber == c.ttlNumber) &&
                (queryNumber == c.queryNumber) &&
                (insertLatency == c.insertLatency) &&
                (deleteLatency == c.deleteLatency) &&
                (updateLatency == c.updateLatency) &&
                (ttlLatency == c.ttlLatency) &&
                (queryLatency == c.queryLatency);
    }
}
