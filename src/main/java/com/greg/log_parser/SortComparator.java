package com.greg.log_parser;

import java.util.Comparator;

class SortComparator implements Comparator<LogData> {

    @Override
    public int compare(LogData logEntry1, LogData logEntry2) {

        int idCompare = logEntry1.getId().compareTo(logEntry2.getId());

        int timeCompare = (int) (logEntry1.getTimestamp() - (logEntry2.getTimestamp()));

        return (idCompare == 0) ? timeCompare : idCompare;
    }
}