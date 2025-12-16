package com.example;

import com.example.parse_module.*;

public class Main {
    public static void main(String[] args) {

        String pgn = """
            [Event "Test Game"]
            [Site "Local"]
            [Date "2025.01.01"]
            [Round "-"]
            [White "Alice"]
            [Black "Bob"]
            [Result "*"]

            1. e4 e5 2. Nf3 Nc6
            """;

        // 1. Bootstrap
        PgnParserService parser = new PgnParserService();
        GameState state = parser.bootstrapFromPgn(pgn);

        System.out.println("After bootstrap:");
        System.out.println(state.getBoard().getFen());

        // 2. Incremental moves
        IncrementalMoveParser incParser = new ChesslibIncrementalMoveParser();

        Position p1 = incParser.applyMove(state, "e4");
        System.out.println(p1);

        Position p2 = incParser.applyMove(state, "e5");
        System.out.println(p2);
    }
}
