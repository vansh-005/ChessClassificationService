package com.example.service_3.model;

public class EngineLine {
    private int id;
    private int depth;
    private Evaluation evaluation;
    private String moveUCI;
    private String moveSAN;

    public EngineLine(int id, int depth, Evaluation evaluation, String moveUCI) {
        this.id = id;
        this.depth = depth;
        this.evaluation = evaluation;
        this.moveUCI = moveUCI;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }
    public String getMoveUCI() { return moveUCI; }
    public void setMoveUCI(String moveUCI) { this.moveUCI = moveUCI; }
    public String getMoveSAN() { return moveSAN; }
    public void setMoveSAN(String moveSAN) { this.moveSAN = moveSAN; }
}
