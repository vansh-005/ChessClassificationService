package com.example.service_3;

//package com.chess.analysis;

import com.example.service_3.model.*;
import com.example.service_3.classification.Classification;
import com.example.service_3.classification.ClassificationService;
import com.example.service_3.board.BoardAnalyzer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.Piece;

import java.util.*;

/**
 * Main service for analyzing chess games and classifying moves.
 * Provides two modes: full game analysis and single move classification.
 */
public class ChessAnalyzer {

    private final ClassificationService classificationService;
    private final BoardAnalyzer boardAnalyzer;
    private final OpeningBook openingBook;

    public ChessAnalyzer(String openingsJsonPath) {
        this.classificationService = new ClassificationService();
        this.boardAnalyzer = new BoardAnalyzer();
        this.openingBook = new OpeningBook(openingsJsonPath);
    }

    /**
     * Analyzes a complete game and generates a comprehensive report.
     *
     * @param positions List of evaluated positions from the game
     * @return Complete analysis report with classifications and statistics
     */
    public Report analyzeGame(List<EvaluatedPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("Positions list cannot be null or empty");
        }

        // Step 1: Classify all moves
        classifyMoves(positions);

        // Step 2: Apply opening names
        applyOpeningNames(positions);

        // Step 3: Apply book move classifications
        applyBookMoves(positions);

        // Step 4: Generate SAN notation for engine lines
        generateSANMoves(positions);

        // Step 5: Calculate statistics
        return generateReport(positions);
    }

    /**
     * Classifies a single move based on current and previous positions.
     *
     * @param currentPosition Current position with evaluation
     * @param previousPosition Previous position with evaluation
     * @return Classification of the move
     */
    public Classification classifyMove(EvaluatedPosition currentPosition,
                                       EvaluatedPosition previousPosition) {
        if (currentPosition == null || previousPosition == null) {
            throw new IllegalArgumentException("Positions cannot be null");
        }

        return classificationService.classifyPosition(
                currentPosition,
                previousPosition,
                boardAnalyzer
        );
    }

    private void classifyMoves(List<EvaluatedPosition> positions) {
        for (int i = 1; i < positions.size(); i++) {
            EvaluatedPosition current = positions.get(i);
            EvaluatedPosition previous = positions.get(i - 1);

            Classification classification = classificationService.classifyPosition(
                    current,
                    previous,
                    boardAnalyzer
            );

            current.setClassification(classification);
        }
    }

    private void applyOpeningNames(List<EvaluatedPosition> positions) {
        for (EvaluatedPosition position : positions) {
            String openingName = openingBook.findOpening(position.getFen());
            if (openingName != null) {
                position.setOpening(openingName);
            }
        }
    }

    private void applyBookMoves(List<EvaluatedPosition> positions) {
        List<Classification> positiveClassifications = Arrays.asList(
                Classification.EXCELLENT,
                Classification.BEST,
                Classification.GREAT,
                Classification.BRILLIANT
        );

        for (int i = 1; i < positions.size(); i++) {
            EvaluatedPosition position = positions.get(i);

            boolean isCloudAndPositive = "cloud".equals(position.getWorker())
                    && positiveClassifications.contains(position.getClassification());

            boolean hasOpening = position.getOpening() != null;

            if (isCloudAndPositive || hasOpening) {
                position.setClassification(Classification.BOOK);
            } else {
                break;
            }
        }
    }

    private void generateSANMoves(List<EvaluatedPosition> positions) {
        for (EvaluatedPosition position : positions) {
            Board board = new Board();
            board.loadFromFen(position.getFen());

            for (EngineLine line : position.getTopLines()) {
                if (line.getEvaluation().isMate() && line.getEvaluation().getValue() == 0) {
                    continue;
                }

                try {
                    String moveUCI = line.getMoveUCI();
                    if (moveUCI != null && moveUCI.length() >= 4) {
                        Square from = Square.fromValue(moveUCI.substring(0, 2).toUpperCase());
                        Square to = Square.fromValue(moveUCI.substring(2, 4).toUpperCase());

                        com.github.bhlangonijr.chesslib.move.Move move =
                                new com.github.bhlangonijr.chesslib.move.Move(from, to);

                        if (moveUCI.length() > 4) {
                            char promotionChar = moveUCI.charAt(4);
                            Piece promotion = convertPromotionCharToPiece(promotionChar, board.getSideToMove());
                            move = new com.github.bhlangonijr.chesslib.move.Move(from, to, promotion);
                        }

                        board.doMove(move);
                        String san = board.getBackup().get(board.getBackup().size() - 1).getMove().toString();
                        line.setMoveSAN(san);
                        board.undoMove();
                    }
                } catch (Exception e) {
                    line.setMoveSAN("");
                }
            }
        }
    }

    private Piece convertPromotionCharToPiece(char c, Side side) {
        switch (Character.toLowerCase(c)) {
            case 'q': return side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            case 'r': return side == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'b': return side == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'n': return side == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default: return side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
        }
    }

    private Report generateReport(List<EvaluatedPosition> positions) {
        Map<String, AccuracyStats> accuracies = new HashMap<>();
        Map<String, ClassificationCount> classifications = new HashMap<>();

        accuracies.put("white", new AccuracyStats());
        accuracies.put("black", new AccuracyStats());
        classifications.put("white", new ClassificationCount());
        classifications.put("black", new ClassificationCount());

        for (int i = 1; i < positions.size(); i++) {
            EvaluatedPosition position = positions.get(i);
            String moveColor = position.getFen().contains(" b ") ? "white" : "black";

            Classification classification = position.getClassification();
            if (classification != null) {
                double value = classification.getAccuracyValue();
                accuracies.get(moveColor).addMove(value);
                classifications.get(moveColor).increment(classification);
            }
        }

        return new Report(
                Map.of(
                        "white", accuracies.get("white").getAccuracy(),
                        "black", accuracies.get("black").getAccuracy()
                ),
                classifications,
                positions
        );
    }

    private static class AccuracyStats {
        private double current = 0;
        private int maximum = 0;

        void addMove(double value) {
            current += value;
            maximum++;
        }

        double getAccuracy() {
            return maximum == 0 ? 0 : (current / maximum) * 100;
        }
    }
}