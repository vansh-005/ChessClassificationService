package com.example.service_3;

import com.example.service_3.classification.Classification;
import com.example.service_3.model.*;

import java.util.ArrayList;
import java.util.List;

public class ClassifySingleMoveExample {

    public static void main(String[] args) {
        ChessAnalyzer analyzer = new ChessAnalyzer("openings.json");

        // Previous position (equal position)
        EvaluatedPosition previous = new EvaluatedPosition(
                "rnbqkbnr/pppppppp/8/8/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 1",
                new Move("Nf3", "g1f3"),
                createPreviousLines(),
                "local"
        );

        // Current position after Qh5?? (queen hangs)
        EvaluatedPosition current = new EvaluatedPosition(
                "rnbqkbnr/pppppppp/8/7Q/4P3/5N2/PPPP1PPP/RNB1KB1R b KQkq - 2 1",
                new Move("Qh5??", "d1h5"),
                createCurrentLines(),
                "local"
        );

        Classification classification = analyzer.classifyMove(current, previous);
        System.out.println("Move Qh5?? classified as: " + classification);
    }

    private static List<EngineLine> createPreviousLines() {
        List<EngineLine> lines = new ArrayList<>();

        lines.add(new EngineLine(
                1, 18,
                new Evaluation(false, 0),
                "e7e5"
        ));

        return lines;
    }

    private static List<EngineLine> createCurrentLines() {
        List<EngineLine> lines = new ArrayList<>();

        // After Qh5?? black can win queen with g6
        lines.add(new EngineLine(
                1, 18,
                new Evaluation(false, -900),
                "g6"
        ));

        return lines;
    }
}
