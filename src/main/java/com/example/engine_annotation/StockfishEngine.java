package com.example.engine_annotation;

import java.io.*;
import java.time.Duration;
import java.util.List;

// Single Process but robust

public class StockfishEngine {

    private final Process process;
    private final BufferedWriter input;
    private final BufferedReader output;

    public StockfishEngine(String path) {
        try {
            process = new ProcessBuilder(path).start();
            input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            output = new BufferedReader(new InputStreamReader(process.getInputStream()));
            init();
        } catch (IOException e) {
            throw new EngineException("Failed to start Stockfish", e);
        }
    }

    private void init() throws IOException {
        send("uci");
        waitFor("uciok", Duration.ofSeconds(2));
        send("isready");
        waitFor("readyok", Duration.ofSeconds(2));
    }

    public List<TopLine> evaluate(String fen, int depth, int multiPv) {
        try {
            send("ucinewgame");
            send("position fen " + fen);
            send("setoption name MultiPV value " + multiPv);
            send("go depth " + depth);

            return UciParser.parse(output, multiPv);

        } catch (IOException e) {
            throw new EngineException("Engine IO failure", e);
        }
    }

    private void send(String cmd) throws IOException {
        input.write(cmd);
        input.newLine();
        input.flush();
    }

    private void waitFor(String token, Duration timeout) throws IOException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        String line;
        while (System.currentTimeMillis() < deadline && (line = output.readLine()) != null) {
            if (line.contains(token)) return;
        }
        throw new EngineException("Timeout waiting for " + token);
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public void shutdown() {
        try {
            send("quit");
        } catch (Exception ignored) {}
        process.destroy();
    }
}
