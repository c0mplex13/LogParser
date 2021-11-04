package com.greg.interview_test;

import java.util.Comparator;

class SortComparator implements Comparator<Keeper> {

    @Override
    public int compare(Keeper entry1, Keeper entry2) {

        int idCompare = entry1.getId().compareTo(entry2.getId());

        int timeCompare = (int) (entry1.getTimestamp() - (entry2.getTimestamp()));

        return (idCompare == 0) ? timeCompare : idCompare;
    }
}