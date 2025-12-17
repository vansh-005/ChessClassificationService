package com.example.engine_annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class UciParser {

    private UciParser() {}

    public static List<TopLine> parse(BufferedReader output, int multiPv) throws IOException {

        Map<Integer, TopLine> lines = new HashMap<>();
        String line;

        while ((line = output.readLine()) != null) {

            if (line.startsWith("info")) {
                parseInfo(line, lines, multiPv);
            }

            if (line.startsWith("bestmove")) {
                break;
            }
        }

        return lines.values()
                .stream()
                .sorted(Comparator.comparingInt(TopLine::getId))
                .collect(Collectors.toList());
    }

    private static void parseInfo(String line,
                                  Map<Integer, TopLine> lines,
                                  int multiPv) {

        // Ignore info lines without PV
        if (!line.contains(" pv ")) return;

        try {
            int pv = extractInt(line, "multipv", 1);
            if (pv < 1 || pv > multiPv) return;

            int depth = extractInt(line, "depth", 0);
            Evaluation eval = extractEval(line);
            String move = extractMove(line);

            if (move.isEmpty()) return;

            TopLine prev = lines.get(pv);
            if (prev == null || depth >= prev.getDepth()) {
                lines.put(pv, new TopLine(pv, depth, eval, move));
            }

        } catch (Exception ignored) {
        }
    }

    private static int extractInt(String line, String key, int def) {
        String[] parts = line.split(" ");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals(key)) {
                return Integer.parseInt(parts[i + 1]);
            }
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

        String pvPart = line.substring(idx + 4).trim();
        int spaceIdx = pvPart.indexOf(' ');
        return spaceIdx == -1 ? pvPart : pvPart.substring(0, spaceIdx);
    }
}
