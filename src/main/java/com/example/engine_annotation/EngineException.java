package com.example.engine_annotation;

public class EngineException extends RuntimeException {
    public EngineException(String message, Exception e) {
        super(message + "  " + e);
    }
    public EngineException(String message) {
        super(message);
    }
}

