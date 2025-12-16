package com.example.engine_annotation;

public final class TopLine {

    private final int id;
    private final int depth;
    private final Evaluation evaluation;
    private final String moveUci;

    public TopLine(int id, int depth, Evaluation evaluation, String moveUci) {
        this.id = id;
        this.depth = depth;
        this.evaluation = evaluation;
        this.moveUci = moveUci;
    }
    @Override
    public String toString() {
        return "TopLine{" +
                "id=" + id +
                ", depth=" + depth +
                ", eval=" + evaluation.getType() + ":" + evaluation.getValue() +
                ", moveUci='" + moveUci + '\'' +
                '}';
    }
}

// Singelton desing pattern