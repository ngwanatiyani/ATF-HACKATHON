package com.example.atfhackathon2026.response;

import java.util.List;

public record BriefResponse(String status, String summary, List<Source> sources) {
    public record Source(String title, String url) {}
}
