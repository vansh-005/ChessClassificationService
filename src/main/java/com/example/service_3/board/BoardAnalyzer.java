package com.example.service_3.board;

//package com.chess.analysis.board;

import com.example.service_3.model.EvaluatedPosition;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.*;

/**
 * Analyzes chess board positions for hanging pieces, attackers, and sacrifices.
 */
public class BoardAnalyzer {

    private static final Map<PieceType, Integer> PIECE_VALUES = Map.of(
            PieceType.PAWN, 1,
            PieceType.KNIGHT, 3,
            PieceType.BISHOP, 3,
            PieceType.ROOK, 5,
            PieceType.QUEEN, 9,
            PieceType.KING, 0
    );

    private static final char[] PROMOTIONS = {'q', 'r', 'b', 'n'};

    /**
     * Checks if a piece on a given square is hanging (undefended or insufficiently defended).
     */
    public boolean isPieceHanging(String lastFen, String currentFen, String squareStr) {
        try {
            Board lastBoard = new Board();
            lastBoard.loadFromFen(lastFen);

            Board currentBoard = new Board();
            currentBoard.loadFromFen(currentFen);

            Square square = Square.fromValue(squareStr.toUpperCase());
            Piece piece = currentBoard.getPiece(square);

            if (piece == Piece.NONE) {
                return false;
            }

            // Get attackers and defenders
            List<Square> attackers = getAttackersOfSquare(currentBoard, square, piece.getPieceSide().flip());
            List<Square> defenders = getAttackersOfSquare(currentBoard, square, piece.getPieceSide());

            if (attackers.isEmpty()) {
                return false;
            }

            // Simple material count - if more attackers than defenders, it's hanging
            return attackers.size() > defenders.size();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds pieces that were sacrificed in the current move.
     */
    public List<Piece> findSacrificedPieces(EvaluatedPosition lastPosition,
                                            EvaluatedPosition currentPosition,
                                            String moveColor) {
        List<Piece> sacrificed = new ArrayList<>();

        try {
            Board lastBoard = new Board();
            lastBoard.loadFromFen(lastPosition.getFen());

            Board currentBoard = new Board();
            currentBoard.loadFromFen(currentPosition.getFen());

            String moveUci = currentPosition.getMove().getUci();
            if (moveUci.length() < 4) {
                return sacrificed;
            }

            Square targetSquare = Square.fromValue(moveUci.substring(2, 4).toUpperCase());
            Piece capturedPiece = lastBoard.getPiece(targetSquare);

            Side side = "white".equals(moveColor) ? Side.WHITE : Side.BLACK;

            // Check all pieces of the moving side
            for (Square square : Square.values()) {
                if (square == Square.NONE) continue;

                Piece piece = currentBoard.getPiece(square);
                if (piece == Piece.NONE || piece.getPieceSide() != side) {
                    continue;
                }

                PieceType type = piece.getPieceType();
                if (type == PieceType.KING || type == PieceType.PAWN) {
                    continue;
                }

                // If captured piece is of higher or equal value, skip
                int capturedValue = PIECE_VALUES.getOrDefault(capturedPiece.getPieceType(), 0);
                int pieceValue = PIECE_VALUES.get(type);

                if (capturedValue >= pieceValue) {
                    continue;
                }

                // Check if this piece is hanging
                if (isPieceHanging(lastPosition.getFen(), currentPosition.getFen(), square.toString().toLowerCase())) {
                    sacrificed.add(piece);
                }
            }
        } catch (Exception e) {
            // Return empty list on error
        }

        return sacrificed;
    }

    /**
     * Checks if sacrificed pieces can be captured without consequences.
     */
    public boolean canCaptureSacrificedPiecesViably(EvaluatedPosition position,
                                                    List<Piece> sacrificedPieces) {
        if (sacrificedPieces.isEmpty()) {
            return false;
        }

        Board board = new Board();
        board.loadFromFen(position.getFen());

        int maxSacrificeValue = sacrificedPieces.stream()
                .mapToInt(p -> PIECE_VALUES.get(p.getPieceType()))
                .max()
                .orElse(0);

        for (Piece sacrificedPiece : sacrificedPieces) {
            Square pieceSquare = findPieceSquare(board, sacrificedPiece);
            if (pieceSquare == null) continue;

            List<Square> attackers = getAttackersOfSquare(board, pieceSquare,
                    sacrificedPiece.getPieceSide().flip());

            for (Square attackerSquare : attackers) {
                // Try each promotion type
                for (char promotion : PROMOTIONS) {
                    try {
                        Board testBoard = new Board();
                        testBoard.loadFromFen(position.getFen());

                        Piece promotionPiece = getPromotionPiece(promotion, testBoard.getSideToMove());
                        Move captureMove = new Move(attackerSquare, pieceSquare, promotionPiece);
                        testBoard.doMove(captureMove);

                        // Check if attacker is pinned
                        boolean attackerPinned = false;
                        for (Square sq : Square.values()) {
                            if (sq == Square.NONE) continue;

                            Piece enemyPiece = testBoard.getPiece(sq);
                            if (enemyPiece == Piece.NONE) continue;
                            if (enemyPiece.getPieceSide() == testBoard.getSideToMove()) continue;

                            PieceType enemyType = enemyPiece.getPieceType();
                            if (enemyType == PieceType.KING || enemyType == PieceType.PAWN) continue;

                            if (isPieceHanging(position.getFen(), testBoard.getFen(), sq.toString().toLowerCase())
                                    && PIECE_VALUES.get(enemyType) >= maxSacrificeValue) {
                                attackerPinned = true;
                                break;
                            }
                        }

                        int pieceValue = PIECE_VALUES.get(sacrificedPiece.getPieceType());

                        // Check conditions for viable capture
                        if (pieceValue >= 5) {
                            if (!attackerPinned) {
                                return true;
                            }
                        } else {
                            boolean hasMateInOne = testBoard.legalMoves().stream()
                                    .anyMatch(m -> m.toString().endsWith("#"));

                            if (!attackerPinned && !hasMateInOne) {
                                return true;
                            }
                        }

                        testBoard.undoMove();
                    } catch (Exception e) {
                        // Continue to next attempt
                    }
                }
            }
        }

        return false;
    }

    private List<Square> getAttackersOfSquare(Board board, Square square, Side attackingSide) {
        List<Square> attackers = new ArrayList<>();

        for (Square from : Square.values()) {
            if (from == Square.NONE) continue;

            Piece piece = board.getPiece(from);
            if (piece == Piece.NONE || piece.getPieceSide() != attackingSide) {
                continue;
            }

            if (board.squareAttackedBy(square, attackingSide) != 0) {
                // Check if this specific piece attacks the square
                long attacks = board.getBitboard(piece);
                if ((attacks & square.getBitboard()) != 0) {
                    attackers.add(from);
                }
            }
        }

        return attackers;
    }

    private Square findPieceSquare(Board board, Piece targetPiece) {
        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;

            Piece piece = board.getPiece(square);
            if (piece.equals(targetPiece)) {
                return square;
            }
        }
        return null;
    }

    private Piece getPromotionPiece(char promotion, Side side) {
        switch (Character.toLowerCase(promotion)) {
            case 'q': return side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            case 'r': return side == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'b': return side == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'n': return side == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default: return side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
        }
    }
}