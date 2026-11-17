package com.example.atfhackathon2026.service;

import com.example.atfhackathon2026.request.ReportRequest;
import com.example.atfhackathon2026.response.BoardResponse;
import com.example.atfhackathon2026.response.ReportResponse;

public interface ReportService {
    BoardResponse board();

    ReportResponse report(String phone, ReportRequest request);
}
