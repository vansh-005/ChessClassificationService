package com.example.service_3.model;

import java.util.List;
import java.util.Map;

public class Report {
    private Map<String, Double> accuracies;
    private Map<String, ClassificationCount> classifications;
    private List<EvaluatedPosition> positions;

    public Report(Map<String, Double> accuracies,
                  Map<String, ClassificationCount> classifications,
                  List<EvaluatedPosition> positions) {
        this.accuracies = accuracies;
        this.classifications = classifications;
        this.positions = positions;
    }

    public Map<String, Double> getAccuracies() { return accuracies; }
    public Map<String, ClassificationCount> getClassifications() { return classifications; }
    public List<EvaluatedPosition> getPositions() { return positions; }
}