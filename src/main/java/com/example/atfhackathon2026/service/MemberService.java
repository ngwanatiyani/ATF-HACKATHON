package com.example.atfhackathon2026.service;

import com.example.atfhackathon2026.request.RegisterRequest;
import com.example.atfhackathon2026.response.MemberResponse;

public interface MemberService {
    void register(RegisterRequest request);

    MemberResponse me(String phone);
}
