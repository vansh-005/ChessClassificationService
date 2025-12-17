package com.example.service_3;

//package com.chess.analysis.classification;

import com.example.service_3.classification.Classification;
import com.example.service_3.model.*;
import com.example.service_3.board.BoardAnalyzer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.Piece;

import java.util.*;

/**
 * Service responsible for classifying chess moves based on evaluation changes
 * and position characteristics.
 */
public class ClassificationService {

    private static final List<Classification> CENTIPAWN_CLASSIFICATIONS = Arrays.asList(
            Classification.BEST,
            Classification.EXCELLENT,
            Classification.GOOD,
            Classification.INACCURACY,
            Classification.MISTAKE,
            Classification.BLUNDER
    );

    /**
     * Calculates the maximum evaluation loss threshold for a given classification.
     * Uses the WTF Algorithm from the original TypeScript code.
     */
    public double getEvaluationLossThreshold(Classification classification, double prevEval) {
        prevEval = Math.abs(prevEval);
        double threshold;

        switch (classification) {
            case BEST:
                threshold = 0.0001 * Math.pow(prevEval, 2) + (0.0236 * prevEval) - 3.7143;
                break;
            case EXCELLENT:
                threshold = 0.0002 * Math.pow(prevEval, 2) + (0.1231 * prevEval) + 27.5455;
                break;
            case GOOD:
                threshold = 0.0002 * Math.pow(prevEval, 2) + (0.2643 * prevEval) + 60.5455;
                break;
            case INACCURACY:
                threshold = 0.0002 * Math.pow(prevEval, 2) + (0.3624 * prevEval) + 108.0909;
                break;
            case MISTAKE:
                threshold = 0.0003 * Math.pow(prevEval, 2) + (0.4027 * prevEval) + 225.8182;
                break;
            default:
                threshold = Double.POSITIVE_INFINITY;
        }

        return Math.max(threshold, 0);
    }

    public Classification classifyPosition(EvaluatedPosition position,
                                           EvaluatedPosition lastPosition,
                                           BoardAnalyzer boardAnalyzer) {
        Board board = new Board();
        board.loadFromFen(position.getFen());

        // Find top moves
        EngineLine topMove = findLineById(lastPosition.getTopLines(), 1);
        EngineLine secondTopMove = findLineById(lastPosition.getTopLines(), 2);

        if (topMove == null) {
            return Classification.BOOK;
        }

        Evaluation previousEvaluation = topMove.getEvaluation();
        Evaluation evaluation = findLineById(position.getTopLines(), 1) != null
                ? findLineById(position.getTopLines(), 1).getEvaluation()
                : null;

        if (previousEvaluation == null) {
            return Classification.BOOK;
        }

        String moveColor = position.getFen().contains(" b ") ? "white" : "black";

        // Handle terminal positions
        if (evaluation == null) {
            evaluation = board.isMated()
                    ? new Evaluation(true, 0)
                    : new Evaluation(false, 0);
            position.getTopLines().add(new EngineLine(1, 0, evaluation, ""));
        }

        // Calculate absolute evaluations
        int colorMultiplier = "white".equals(moveColor) ? 1 : -1;
        double absoluteEvaluation = evaluation.getValue() * colorMultiplier;
        double previousAbsoluteEvaluation = previousEvaluation.getValue() * colorMultiplier;
        double absoluteSecondEvaluation = secondTopMove != null
                ? secondTopMove.getEvaluation().getValue() * colorMultiplier
                : 0;

        // Calculate evaluation loss
        double evalLoss = calculateEvaluationLoss(
                position, lastPosition, topMove,
                previousEvaluation, evaluation, moveColor
        );

        // If only one legal move, it's forced
        if (secondTopMove == null) {
            return Classification.FORCED;
        }

        boolean noMate = !previousEvaluation.isMate() && !evaluation.isMate();

        // If the move played matches the top engine move
        if (topMove.getMoveUCI().equals(position.getMove().getUci())) {
            return Classification.BEST;
        }

        // Classify based on evaluation type changes
        Classification classification = classifyByEvaluationType(
                noMate, previousEvaluation, evaluation,
                absoluteEvaluation, previousAbsoluteEvaluation,
                evalLoss
        );

        // Check for brilliancy
        if (classification == Classification.BEST) {
            classification = checkForBrilliancy(
                    position, lastPosition, board,
                    absoluteEvaluation, absoluteSecondEvaluation,
                    topMove, secondTopMove, moveColor, boardAnalyzer
            );
        }

        // Check for great move
        if (classification == Classification.BEST) {
            classification = checkForGreatMove(
                    position, lastPosition, topMove, secondTopMove,
                    noMate, boardAnalyzer
            );
        }

        // Adjust blunder classifications
        classification = adjustBlunderClassification(
                classification, absoluteEvaluation,
                previousAbsoluteEvaluation, previousEvaluation, evaluation
        );

        return classification != null ? classification : Classification.BOOK;
    }

    private double calculateEvaluationLoss(EvaluatedPosition position,
                                           EvaluatedPosition lastPosition,
                                           EngineLine topMove,
                                           Evaluation previousEvaluation,
                                           Evaluation evaluation,
                                           String moveColor) {
        double evalLoss = Double.POSITIVE_INFINITY;
        double cutoffEvalLoss = Double.POSITIVE_INFINITY;
        double lastLineEvalLoss = Double.POSITIVE_INFINITY;

        int colorMultiplier = "white".equals(moveColor) ? 1 : -1;

        // Check if played move matches any engine line
        EngineLine matchingLine = lastPosition.getTopLines().stream()
                .filter(line -> line.getMoveUCI().equals(position.getMove().getUci()))
                .findFirst()
                .orElse(null);

        if (matchingLine != null) {
            lastLineEvalLoss = Math.abs(
                    (previousEvaluation.getValue() - matchingLine.getEvaluation().getValue()) * colorMultiplier
            );
        }

        // Check cutoff evaluation
        if (lastPosition.getCutoffEvaluation() != null) {
            cutoffEvalLoss = Math.abs(
                    (lastPosition.getCutoffEvaluation().getValue() - evaluation.getValue()) * colorMultiplier
            );
        }

        // Direct evaluation loss
        evalLoss = Math.abs(
                (previousEvaluation.getValue() - evaluation.getValue()) * colorMultiplier
        );

        return Math.min(Math.min(evalLoss, cutoffEvalLoss), lastLineEvalLoss);
    }

    private Classification classifyByEvaluationType(boolean noMate,
                                                    Evaluation previousEvaluation,
                                                    Evaluation evaluation,
                                                    double absoluteEvaluation,
                                                    double previousAbsoluteEvaluation,
                                                    double evalLoss) {
        // No mate in either position
        if (noMate) {
            for (Classification classif : CENTIPAWN_CLASSIFICATIONS) {
                if (evalLoss <= getEvaluationLossThreshold(classif, previousEvaluation.getValue())) {
                    return classif;
                }
            }
        }

        // No mate before, but mate now
        if (!previousEvaluation.isMate() && evaluation.isMate()) {
            if (absoluteEvaluation > 0) {
                return Classification.BEST;
            } else if (absoluteEvaluation >= -2) {
                return Classification.BLUNDER;
            } else if (absoluteEvaluation >= -5) {
                return Classification.MISTAKE;
            } else {
                return Classification.INACCURACY;
            }
        }

        // Mate before, no mate now
        if (previousEvaluation.isMate() && !evaluation.isMate()) {
            if (previousAbsoluteEvaluation < 0 && absoluteEvaluation < 0) {
                return Classification.BEST;
            } else if (absoluteEvaluation >= 400) {
                return Classification.GOOD;
            } else if (absoluteEvaluation >= 150) {
                return Classification.INACCURACY;
            } else if (absoluteEvaluation >= -100) {
                return Classification.MISTAKE;
            } else {
                return Classification.BLUNDER;
            }
        }

        // Both positions have mate
        if (previousEvaluation.isMate() && evaluation.isMate()) {
            if (previousAbsoluteEvaluation > 0) {
                if (absoluteEvaluation <= -4) {
                    return Classification.MISTAKE;
                } else if (absoluteEvaluation < 0) {
                    return Classification.BLUNDER;
                } else if (absoluteEvaluation < previousAbsoluteEvaluation) {
                    return Classification.BEST;
                } else if (absoluteEvaluation <= previousAbsoluteEvaluation + 2) {
                    return Classification.EXCELLENT;
                } else {
                    return Classification.GOOD;
                }
            } else {
                if (absoluteEvaluation == previousAbsoluteEvaluation) {
                    return Classification.BEST;
                } else {
                    return Classification.GOOD;
                }
            }
        }

        return Classification.GOOD;
    }

    private Classification checkForBrilliancy(EvaluatedPosition position,
                                              EvaluatedPosition lastPosition,
                                              Board board,
                                              double absoluteEvaluation,
                                              double absoluteSecondEvaluation,
                                              EngineLine topMove,
                                              EngineLine secondTopMove,
                                              String moveColor,
                                              BoardAnalyzer boardAnalyzer) {
        // Must be winning for the side that played
        boolean winningAnyways = (absoluteSecondEvaluation >= 700 && !topMove.getEvaluation().isMate())
                || (topMove.getEvaluation().isMate() && secondTopMove != null && secondTopMove.getEvaluation().isMate());

        if (absoluteEvaluation < 0 || winningAnyways || position.getMove().getSan().contains("=")) {
            return Classification.BEST;
        }

        Board lastBoard = new Board();
        lastBoard.loadFromFen(lastPosition.getFen());

        if (lastBoard.isKingAttacked()) {
            return Classification.BEST;
        }

        // Check for sacrificed pieces
        List<Piece> sacrificedPieces = boardAnalyzer.findSacrificedPieces(
                lastPosition, position, moveColor
        );

        if (sacrificedPieces.isEmpty()) {
            return Classification.BEST;
        }

        // Check if sacrificed pieces can be captured viably
        boolean anyPieceViablyCapturable = boardAnalyzer.canCaptureSacrificedPiecesViably(
                position, sacrificedPieces
        );

        return anyPieceViablyCapturable ? Classification.BEST : Classification.BRILLIANT;
    }

    private Classification checkForGreatMove(EvaluatedPosition position,
                                             EvaluatedPosition lastPosition,
                                             EngineLine topMove,
                                             EngineLine secondTopMove,
                                             boolean noMate,
                                             BoardAnalyzer boardAnalyzer) {
        try {
            if (noMate
                    && lastPosition.getClassification() == Classification.BLUNDER
                    && Math.abs(topMove.getEvaluation().getValue() - secondTopMove.getEvaluation().getValue()) >= 150) {

                String targetSquare = position.getMove().getUci().substring(2, 4);
                if (!boardAnalyzer.isPieceHanging(lastPosition.getFen(), position.getFen(), targetSquare)) {
                    return Classification.GREAT;
                }
            }
        } catch (Exception e) {
            // Ignore errors in great move detection
        }

        return Classification.BEST;
    }

    private Classification adjustBlunderClassification(Classification classification,
                                                       double absoluteEvaluation,
                                                       double previousAbsoluteEvaluation,
                                                       Evaluation previousEvaluation,
                                                       Evaluation evaluation) {
        // Don't allow blunder if still completely winning
        if (classification == Classification.BLUNDER && absoluteEvaluation >= 600) {
            return Classification.GOOD;
        }

        // Don't allow blunder if already in completely lost position
        if (classification == Classification.BLUNDER
                && previousAbsoluteEvaluation <= -600
                && !previousEvaluation.isMate()
                && !evaluation.isMate()) {
            return Classification.GOOD;
        }

        return classification;
    }

    private EngineLine findLineById(List<EngineLine> lines, int id) {
        return lines.stream()
                .filter(line -> line.getId() == id)
                .findFirst()
                .orElse(null);
    }
}