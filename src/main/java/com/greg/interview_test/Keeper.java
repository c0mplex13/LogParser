package com.greg.interview_test;

public class Keeper {

    private String id;
    private String state;
    private String type;
    private String host;
    private long timestamp;

    public Keeper() {
    }

    public Keeper(String id, String state, long timestamp) {
        this.id = id;
        this.state = state;
        this.timestamp = timestamp;
    }

    public Keeper(String id, String state, String type, String host, long timestamp) {
        this.id = id;
        this.state = state;
        this.type = type;
        this.host = host;
        this.timestamp = timestamp;
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return id + " " + state + " " + type + " " + host + " " + timestamp + "\n";
    }
}
