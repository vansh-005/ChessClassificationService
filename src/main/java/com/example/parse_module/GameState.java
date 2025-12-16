package com.example.parse_module;

import com.github.bhlangonijr.chesslib.Board;

public class GameState {

    private final Board board;
    private int moveCount;

    public GameState(Board board, int moveCount) {
        this.board = board;
        this.moveCount = moveCount;
    }

    public Board getBoard() {
        return board;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        this.moveCount++;
    }
}
