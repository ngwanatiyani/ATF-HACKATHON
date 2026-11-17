package com.example.atfhackathon2026.service;

import com.example.atfhackathon2026.domain.Member;
import com.example.atfhackathon2026.domain.Report;
import com.example.atfhackathon2026.factory.ReportFactory;
import com.example.atfhackathon2026.helper.Helper;
import com.example.atfhackathon2026.repository.MemberRepository;
import com.example.atfhackathon2026.repository.ReportRepository;
import com.example.atfhackathon2026.request.ReportRequest;
import com.example.atfhackathon2026.response.BoardResponse;
import com.example.atfhackathon2026.response.ReportResponse;
import com.example.atfhackathon2026.response.RouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private final MemberRepository members;
    private final ReportRepository reports;

    public ReportServiceImpl(MemberRepository members, ReportRepository reports) {
        this.members = members;
        this.reports = reports;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse board() {
        List<Report> rows = reports.findAllByOrderByIdDesc();
        var grouped = new LinkedHashMap<String, List<Report>>();
        for (Report row : rows) grouped.computeIfAbsent(row.getRoute(), key -> new ArrayList<>()).add(row);
        List<RouteResponse> routes = grouped.entrySet().stream()
                .map(entry -> score(entry.getKey(), entry.getValue()))
                .toList();
        List<ReportResponse> latest = rows.stream().limit(40).map(ReportServiceImpl::toResponse).toList();
        return new BoardResponse(routes, latest);
    }

    @Override
    public ReportResponse report(String phone, ReportRequest request) {
        String kind = request == null || request.kind() == null ? "" : request.kind().trim().toUpperCase();
        if (request == null || Helper.isNullOrEmpty(request.route()) || !Helper.PENALTY.containsKey(kind))
            throw new IllegalArgumentException("Route and a report type are required");
        if (request.minutes() != null && (request.minutes() < 0 || request.minutes() > 240))
            throw new IllegalArgumentException("Wait must be between 0 and 240 minutes");
        Member member = members.findByPhone(phone).orElseThrow();
        Report report = ReportFactory.build(member, request.route(), kind, request.note(), request.minutes());
        if (report == null)
            throw new IllegalArgumentException("Route and a report type are required");
        return toResponse(reports.save(report));
    }

    private static RouteResponse score(String name, List<Report> rows) {
        int score = 100;
        for (Report row : rows.stream().limit(5).toList()) score -= Helper.PENALTY.get(row.getKind());
        int[] waits = rows.stream().map(Report::getMinutes).filter(minutes -> minutes != null).mapToInt(Integer::intValue).toArray();
        Integer avg = waits.length == 0 ? null : (int) Math.round(java.util.Arrays.stream(waits).average().orElse(0));
        int shown = Math.max(0, score);
        return new RouteResponse(name, shown, Helper.label(shown), rows.size(), avg);
    }

    private static ReportResponse toResponse(Report report) {
        return new ReportResponse(report.getId(), report.getRoute(), report.getKind(), report.getNote(),
                report.getMinutes(), report.getMember().getName(), report.getAt());
    }
}
