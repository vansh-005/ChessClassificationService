package com.example.parse_module;

public class ChesslibIncrementalMoveParser implements IncrementalMoveParser {

    @Override
    public Position applyMove(GameState state, String sanMove) {

        try {
            boolean success = state.getBoard().doMove(sanMove);

            if (!success) {
                throw new IllegalMoveException("Illegal move: " + sanMove);
            }

            state.incrementMoveCount();

            return new Position(
                    state.getBoard().getFen(),
                    new ParsedMove(
                            sanMove,
                            null   // UCI not available in chesslib 1.3.4 for SAN moves
                    )
            );

        } catch (Exception e) {
            throw new IllegalMoveException("Invalid SAN: " + sanMove);
        }
    }
}
