package com.example.atfhackathon2026.service;

import com.example.atfhackathon2026.domain.Member;
import com.example.atfhackathon2026.factory.MemberFactory;
import com.example.atfhackathon2026.helper.Helper;
import com.example.atfhackathon2026.repository.MemberRepository;
import com.example.atfhackathon2026.request.RegisterRequest;
import com.example.atfhackathon2026.response.MemberResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository members;
    private final PasswordEncoder encoder;

    public MemberServiceImpl(MemberRepository members, PasswordEncoder encoder) {
        this.members = members;
        this.encoder = encoder;
    }

    @Override
    public void register(RegisterRequest request) {
        if (request == null || Helper.isNullOrEmpty(request.name()) || Helper.isNullOrEmpty(request.phone())
                || request.password() == null || request.password().length() < 4)
            throw new IllegalArgumentException("Name, phone, and a password of 4 or more characters");
        String phone = request.phone().trim();
        if (members.findByPhone(phone).isPresent())
            throw new IllegalStateException("That phone is already on RoutePulse");
        Member member = MemberFactory.build(request.name(), phone, encoder.encode(request.password()));
        if (member == null)
            throw new IllegalArgumentException("Name, phone, and a password of 4 or more characters");
        members.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse me(String phone) {
        Member member = members.findByPhone(phone).orElseThrow();
        return new MemberResponse(member.getName());
    }
}
