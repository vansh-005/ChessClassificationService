package com.example.service_3.model;

import com.example.service_3.classification.Classification;

public class ClassificationCount {
    private int brilliant = 0;
    private int great = 0;
    private int best = 0;
    private int excellent = 0;
    private int good = 0;
    private int inaccuracy = 0;
    private int mistake = 0;
    private int blunder = 0;
    private int book = 0;
    private int forced = 0;

    public void increment(Classification classification) {
        switch (classification) {
            case BRILLIANT: brilliant++; break;
            case GREAT: great++; break;
            case BEST: best++; break;
            case EXCELLENT: excellent++; break;
            case GOOD: good++; break;
            case INACCURACY: inaccuracy++; break;
            case MISTAKE: mistake++; break;
            case BLUNDER: blunder++; break;
            case BOOK: book++; break;
            case FORCED: forced++; break;
        }
    }

    // Getters
    public int getBrilliant() { return brilliant; }
    public int getGreat() { return great; }
    public int getBest() { return best; }
    public int getExcellent() { return excellent; }
    public int getGood() { return good; }
    public int getInaccuracy() { return inaccuracy; }
    public int getMistake() { return mistake; }
    public int getBlunder() { return blunder; }
    public int getBook() { return book; }
    public int getForced() { return forced; }
}
