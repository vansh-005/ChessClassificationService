package com.example.service_3.model;

import com.example.service_3.classification.Classification;

import java.util.ArrayList;
import java.util.List;

public class EvaluatedPosition {
    private String fen;
    private Move move;
    private List<EngineLine> topLines;
    private Evaluation cutoffEvaluation;
    private Classification classification;
    private String opening;
    private String worker;

    public EvaluatedPosition(String fen, Move move, List<EngineLine> topLines, String worker) {
        this.fen = fen;
        this.move = move;
        this.topLines = topLines != null ? topLines : new ArrayList<>();
        this.worker = worker;
    }

    // Getters and setters
    public String getFen() { return fen; }
    public void setFen(String fen) { this.fen = fen; }
    public Move getMove() { return move; }
    public void setMove(Move move) { this.move = move; }
    public List<EngineLine> getTopLines() { return topLines; }
    public void setTopLines(List<EngineLine> topLines) { this.topLines = topLines; }
    public Evaluation getCutoffEvaluation() { return cutoffEvaluation; }
    public void setCutoffEvaluation(Evaluation cutoffEvaluation) { this.cutoffEvaluation = cutoffEvaluation; }
    public Classification getClassification() { return classification; }
    public void setClassification(Classification classification) { this.classification = classification; }
    public String getOpening() { return opening; }
    public void setOpening(String opening) { this.opening = opening; }
    public String getWorker() { return worker; }
    public void setWorker(String worker) { this.worker = worker; }
}
