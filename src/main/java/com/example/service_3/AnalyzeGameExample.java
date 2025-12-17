package com.example.service_3;


import com.example.service_3.model.*;
import java.util.*;

public class AnalyzeGameExample {
    public static void main(String[] args) {
        // Initialize analyzer with path to openings.json
        ChessAnalyzer analyzer = new ChessAnalyzer("openings.json");

        // Create list of evaluated positions (from your engine)
        List<EvaluatedPosition> positions = new ArrayList<>();

        // Add starting position
        EvaluatedPosition startPos = new EvaluatedPosition(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                null,
                createEngineLines(),
                "local"
        );
        positions.add(startPos);

        // Add subsequent positions with moves
        EvaluatedPosition afterE4 = new EvaluatedPosition(
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
                new Move("e4", "e2e4"),
                createEngineLines(),
                "local"
        );
        positions.add(afterE4);

        // Analyze the game
        Report report = analyzer.analyzeGame(positions);

        // Access results
        System.out.println("White Accuracy: " + report.getAccuracies().get("white") + "%");
        System.out.println("Black Accuracy: " + report.getAccuracies().get("black") + "%");

        ClassificationCount whiteStats = report.getClassifications().get("white");
        System.out.println("White Brilliant moves: " + whiteStats.getBrilliant());
        System.out.println("White Blunders: " + whiteStats.getBlunder());

        // Iterate through classified positions
        for (EvaluatedPosition pos : report.getPositions()) {
            if (pos.getClassification() != null) {
                System.out.println(
                        pos.getMove().getSan() + " - " + pos.getClassification()
                );
            }
        }
    }

    private static List<EngineLine> createEngineLines() {
        List<EngineLine> lines = new ArrayList<>();
        lines.add(new EngineLine(1, 20, new Evaluation(false, 20), "e2e4"));
        lines.add(new EngineLine(2, 20, new Evaluation(false, 15), "d2d4"));
        return lines;
    }
}