package com.example.parse_module;

public final class ParsedMove {

    private final String san;
    private final String uci; // may be null

    public ParsedMove(String san, String uci) {
        this.san = san;
        this.uci = uci;
    }

    public String getSan() {
        return san;
    }

    public String getUci() {
        return uci;
    }

    @Override
    public String toString() {
        return uci == null
                ? "Move[san=" + san + "]"
                : "Move[san=" + san + ", uci=" + uci + "]";
    }
}
