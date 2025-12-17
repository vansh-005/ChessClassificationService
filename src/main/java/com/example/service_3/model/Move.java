package com.example.service_3.model;

//package com.chess.analysis.model;

import com.example.service_3.classification.Classification;
import java.util.*;

// ========== Move.java ==========
public class Move {
    private String san;
    private String uci;

    public Move(String san, String uci) {
        this.san = san;
        this.uci = uci;
    }

    public String getSan() { return san; }
    public void setSan(String san) { this.san = san; }
    public String getUci() { return uci; }
    public void setUci(String uci) { this.uci = uci; }
}
