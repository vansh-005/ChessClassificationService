package com.example.engine_annotation;

import java.util.List;

// this is the main service
public class EvaluationService {

    private final StockfishEnginePool pool;

    public EvaluationService(StockfishEnginePool pool) {
        this.pool = pool;
    }

    public List<TopLine> evaluatePosition(String fen) {
        StockfishEngine engine = null;
        try {
            engine = pool.borrow();
            return engine.evaluate(fen, 18, 3);
        } catch (InterruptedException e) {
            throw new EngineException("Interrupted");
        } finally {
            if (engine != null) pool.release(engine);
        }
    }
}
