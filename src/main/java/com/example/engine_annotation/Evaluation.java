package com.example.engine_annotation;

public final class Evaluation {
    private final String type; // "cp" or "mate"
    private final int value;

    public Evaluation(String type, int value) {
        this.type = type;
        this.value = value;
    }

    public String getType() { return type; }
    public int getValue() { return value; }
}
