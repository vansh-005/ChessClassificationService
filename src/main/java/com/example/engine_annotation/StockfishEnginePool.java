package com.example.engine_annotation;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// this is the connection pool
public class StockfishEnginePool {

    private final BlockingQueue<StockfishEngine> pool;

    public StockfishEnginePool(int size, String path) {
        pool = new ArrayBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            pool.add(new StockfishEngine(path));
        }
    }

    public StockfishEngine borrow() throws InterruptedException {
        StockfishEngine engine = pool.take();
        if (!engine.isAlive()) {
            return new StockfishEngine("stockfish");
        }
        return engine;
    }

    public void release(StockfishEngine engine) {
        pool.offer(engine);
    }
}
