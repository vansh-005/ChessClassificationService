package com.example.engine_annotation;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        StockfishEnginePool pool = new StockfishEnginePool(4,"C:\\stockfish\\stockfish-windows-x86-64-avx2.exe");
        EvaluationService service = new EvaluationService(pool);
        String fen = "rnb1kbnr/pppp1ppp/8/4p1q1/4PP2/8/PPPP2PP/RNBQKBNR w KQkq - 1 3";
        List<TopLine> lines = service.evaluatePosition(fen);

        for (TopLine line : lines) {
            System.out.println(line);
        }
    }
}
