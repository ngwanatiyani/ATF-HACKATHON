package com.example.atfhackathon2026.response;

import java.time.Instant;

public record ReportResponse(long id, String route, String kind, String note, Integer minutes, String by, Instant at) {}
