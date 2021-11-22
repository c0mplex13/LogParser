package com.greg.log_parser;

public class LogData {

    private String id;
    private String state;
    private String type;
    private String host;
    private long timestamp;

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return id + " " + state + " " + type + " " + host + " " + timestamp + "\n";
    }
}
