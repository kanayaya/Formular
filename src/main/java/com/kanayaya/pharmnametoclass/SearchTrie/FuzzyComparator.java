package com.kanayaya.pharmnametoclass.SearchTrie;

public interface FuzzyComparator {
    public boolean isCloserThan(String piece, int difference);
    int decreaseDifference(int letters);
    int getMaxDistance();
}
