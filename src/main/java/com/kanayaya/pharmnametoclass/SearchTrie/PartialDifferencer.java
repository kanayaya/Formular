package com.kanayaya.pharmnametoclass.SearchTrie;

public interface PartialDifferencer {
    int increaseDifference(String piece);

    int decreaseDifference(String piece);

    int getMaxDistance();

    int distance(String piece);
    int remainingSize();
}
