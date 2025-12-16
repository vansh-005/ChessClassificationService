package com.example.parse_module;

public final class Position {

    private final String fen;
    private final ParsedMove move;

    public Position(String fen) {
        this(fen, null);
    }

    public Position(String fen, ParsedMove move) {
        this.fen = fen;
        this.move = move;
    }

    public String getFen() {
        return fen;
    }

    public ParsedMove getMove() {
        return move;
    }

    @Override
    public String toString() {
        if (move == null) {
            return "Position{initial, fen=" + fen + "}";
        }
        return "Position{fen=" + fen + ", move=" + move + "}";
    }
}
