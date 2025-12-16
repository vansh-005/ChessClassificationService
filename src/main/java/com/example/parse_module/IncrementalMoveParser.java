package com.example.parse_module;

public interface IncrementalMoveParser {

    Position applyMove(GameState state, String sanMove);
}
