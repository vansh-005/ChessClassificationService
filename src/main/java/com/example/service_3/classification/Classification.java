package com.example.service_3.classification;

//package com.chess.analysis.classification;

public enum Classification {
    BRILLIANT("brilliant", 1.0),
    GREAT("great", 1.0),
    BEST("best", 1.0),
    EXCELLENT("excellent", 0.9),
    GOOD("good", 0.65),
    INACCURACY("inaccuracy", 0.4),
    MISTAKE("mistake", 0.2),
    BLUNDER("blunder", 0.0),
    BOOK("book", 1.0),
    FORCED("forced", 1.0);

    private final String value;
    private final double accuracyValue;

    Classification(String value, double accuracyValue) {
        this.value = value;
        this.accuracyValue = accuracyValue;
    }

    public String getValue() {
        return value;
    }

    public double getAccuracyValue() {
        return accuracyValue;
    }

//    public double getValue() {
//        return accuracyValue;
//    }

    @Override
    public String toString() {
        return value;
    }
}