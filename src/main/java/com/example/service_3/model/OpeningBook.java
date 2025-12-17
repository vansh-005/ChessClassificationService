package com.example.service_3.model;


//package com.chess.analysis.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages chess opening database for identifying named positions.
 */
public class OpeningBook {

    private final List<Opening> openings;

    public OpeningBook(String jsonPath) {
        this.openings = loadOpenings(jsonPath);
    }

    /**
     * Finds the opening name for a given FEN position.
     *
     * @param fen The FEN string of the position
     * @return The opening name if found, null otherwise
     */
    public String findOpening(String fen) {
        if (fen == null) {
            return null;
        }

        for (Opening opening : openings) {
            if (fen.contains(opening.getFen())) {
                return opening.getName();
            }
        }

        return null;
    }

    private List<Opening> loadOpenings(String path) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Opening>>(){}.getType();

            // Try loading from file system first
            try (FileReader reader = new FileReader(path)) {
                return gson.fromJson(reader, listType);
            } catch (IOException e) {
                // Try loading from classpath
                InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                if (is != null) {
                    try (InputStreamReader reader = new InputStreamReader(is)) {
                        return gson.fromJson(reader, listType);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load openings: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * Internal class representing a chess opening.
     */
    private static class Opening {
        private String name;
        private String fen;

        public String getName() {
            return name;
        }

        public String getFen() {
            return fen;
        }
    }
}