package com.example.engine_annotation;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        StockfishEnginePool pool = new StockfishEnginePool(4,"C:\\stockfish\\stockfish-windows-x86-64-avx2.exe");
        EvaluationService service = new EvaluationService(pool);
        String fen = "r1b1k2r/pp2bppp/n1p2n2/4q3/8/2Np2P1/PPN1PPBP/R1BQR1K1 b kq - 1 11";
        List<TopLine> lines = service.evaluatePosition(fen);

        for (TopLine line : lines) {
            System.out.println(line);
        }
    }
}
