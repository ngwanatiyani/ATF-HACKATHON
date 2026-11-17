package com.example.atfhackathon2026.service;

import com.example.atfhackathon2026.request.BriefRequest;
import com.example.atfhackathon2026.response.BriefResponse;

public interface BriefService {
    BriefResponse check(BriefRequest request);
}
