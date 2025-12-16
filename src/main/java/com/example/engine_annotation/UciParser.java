package com.example.engine_annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public final class UciParser {

    public static List<TopLine> parse(BufferedReader output, int multiPv) throws IOException {

        Map<Integer, TopLine> lines = new HashMap<>();
        String line;

        while ((line = output.readLine()) != null) {
            if (line.startsWith("info")) {
                parseInfo(line, lines);
            }
            if (line.startsWith("bestmove")) break;
        }

        return new ArrayList<>(lines.values());
    }

    private static void parseInfo(String line, Map<Integer, TopLine> lines) {
        try {
            int pv = extractInt(line, "multipv", 1);
            int depth = extractInt(line, "depth", 0);
            Evaluation eval = extractEval(line);
            String move = extractMove(line);

            lines.put(pv, new TopLine(pv, depth, eval, move));

        } catch (Exception ignored) {}
    }

    private static int extractInt(String line, String key, int def) {
        String[] parts = line.split(" ");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals(key)) return Integer.parseInt(parts[i + 1]);
        }
        return def;
    }

    private static Evaluation extractEval(String line) {
        if (line.contains("score mate")) {
            int mate = extractInt(line, "mate", 0);
            return new Evaluation("mate", mate);
        }
        int cp = extractInt(line, "cp", 0);
        return new Evaluation("cp", cp);
    }

    private static String extractMove(String line) {
        int idx = line.indexOf(" pv ");
        if (idx == -1) return "";
        return line.substring(idx + 4).split(" ")[0];
    }
}
