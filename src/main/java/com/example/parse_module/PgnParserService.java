package com.example.parse_module;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.nio.file.Files;
import java.nio.file.Path;

public class PgnParserService {
    // still need to work on uci
    public GameState bootstrapFromPgn(String pgn) {

        if (pgn == null || pgn.isBlank()) {
            throw new InvalidPgnException("PGN is empty");
        }

        MoveList moves = parseMoves(pgn);
        Board board = new Board();

        for (Move move : moves) {
            if (!board.isMoveLegal(move, true)) {
                throw new IllegalMoveException("Illegal move in PGN");
            }
            board.doMove(move);
        }

        return new GameState(board, moves.size());
    }

    private MoveList parseMoves(String pgn) {
        try {
            Path tempFile = Files.createTempFile("game-", ".pgn");
            Files.writeString(tempFile, pgn);

            PgnHolder holder = new PgnHolder(tempFile.toString());
            holder.loadPgn();

            if (holder.getGames().isEmpty()) {
                throw new InvalidPgnException("Invalid PGN");
            }

            return holder.getGames().get(0).getHalfMoves();

        } catch (Exception e) {
            throw new InvalidPgnException("Failed to parse PGN");
        }
    }
}
