package com.example.service_3.model;

public class Evaluation {
    private boolean mate;
    private double value;

    public Evaluation(boolean mate, double value) {
        this.mate = mate;
        this.value = value;
    }

    public boolean isMate() { return mate; }
    public void setMate(boolean mate) { this.mate = mate; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
