package com.example.atfhackathon2026.controller;

import com.example.atfhackathon2026.request.ReportRequest;
import com.example.atfhackathon2026.response.BoardResponse;
import com.example.atfhackathon2026.response.ReportResponse;
import com.example.atfhackathon2026.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ReportController {
    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/board")
    public BoardResponse board() {
        return reports.board();
    }

    @PostMapping("/reports")
    public ReportResponse report(Principal principal, @RequestBody ReportRequest request) {
        try {
            return reports.report(principal.getName(), request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
