package com.example.atfhackathon2026.helper;

import java.util.Map;

public class Helper {
    public static final Map<String, Integer> PENALTY = Map.of(
            "DELAY", 20,
            "QUEUE", 15,
            "DISRUPTION", 35,
            "CHANGE", 30,
            "CLOSED", 50);

    private Helper() {}

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }

    public static String label(int score) {
        if (score >= 70) return "High";
        if (score >= 40) return "Medium";
        return "Low";
    }
}
