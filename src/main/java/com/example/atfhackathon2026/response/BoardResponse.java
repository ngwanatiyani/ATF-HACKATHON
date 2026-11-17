package com.example.atfhackathon2026.response;

import java.util.List;

public record BoardResponse(List<RouteResponse> routes, List<ReportResponse> latest) {}
