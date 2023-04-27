package com.kanayaya.pharmnametoclass.SearchTrie;

import java.util.ArrayList;

public class PartialLevenstein implements PartialDifferencer {
    private final ArrayList<ArrayList<Integer>> dp;
    private final String inMemory;



    public PartialLevenstein(String s) {
        this.inMemory = s;
        dp = new ArrayList<>();
        dp.add(new ArrayList<>(s.length() + 1));
        for (int j = 0; j <= s.length(); j++) {
            dp.get(0).add(j);
        }
    }

    @Override
    public int increaseDifference(String piece) {
        return distance(piece);
    }

    @Override
    public int decreaseDifference(String piece) {
        for (int i = 1; i <= piece.length(); i++) {
            dp.remove(dp.size() - 1);
        }
        int coordinate = Math.min(dp.size()-1, inMemory.length());
        return dp.get(coordinate).get(coordinate);
    }

    @Override
    public int getMaxDistance() {
        return dp.get(dp.size()-1).get(dp.get(0).size()-1);
    }


    @Override
    public int distance(String piece) {
        int prevLastIndex = dp.size() - 1;
        for (int i = 1; i <= piece.length(); i++) {
            dp.add(new ArrayList<>(inMemory.length() + 1));
            dp.get(prevLastIndex + i).add(i);
            for (int j = 1; j <= inMemory.length(); j++) {
                int cost = piece.charAt(i - 1) == inMemory.charAt(j - 1) ? 0 : 1;
                dp.get(prevLastIndex + i)
                        .add(Math.min(
                                dp.get(prevLastIndex + i - 1).get(j - 1) + cost,
                                Math.min(
                                        dp.get(prevLastIndex + i - 1).get(j),
                                        dp.get(prevLastIndex + i).get(j - 1)) + 1));
            }
        }
        int coordinate = Math.min(prevLastIndex + piece.length(), inMemory.length());
        return dp.get(coordinate)
                .get(coordinate);
    }

    @Override
    public int remainingSize() {
        return inMemory.length() - (dp.size()-1);
    }
}
