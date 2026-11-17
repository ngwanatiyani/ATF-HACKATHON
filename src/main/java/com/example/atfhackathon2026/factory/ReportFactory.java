package com.example.atfhackathon2026.factory;

import com.example.atfhackathon2026.domain.Member;
import com.example.atfhackathon2026.domain.Report;
import com.example.atfhackathon2026.helper.Helper;

import java.time.Instant;

public class ReportFactory {
    private ReportFactory() {}

    public static Report build(Member member, String route, String kind, String note, Integer minutes) {
        if (member == null || Helper.isNullOrEmpty(route) || kind == null || !Helper.PENALTY.containsKey(kind))
            return null;
        Report report = new Report();
        report.setMember(member);
        report.setRoute(route.trim());
        report.setKind(kind);
        report.setNote(Helper.isNullOrEmpty(note) ? null : note.trim());
        report.setMinutes(minutes);
        report.setAt(Instant.now());
        return report;
    }
}
