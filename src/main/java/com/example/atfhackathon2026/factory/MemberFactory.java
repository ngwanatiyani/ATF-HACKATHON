package com.example.atfhackathon2026.factory;

import com.example.atfhackathon2026.domain.Member;
import com.example.atfhackathon2026.helper.Helper;

public class MemberFactory {
    private MemberFactory() {}

    public static Member build(String name, String phone, String encodedPassword) {
        if (Helper.isNullOrEmpty(name) || Helper.isNullOrEmpty(phone) || Helper.isNullOrEmpty(encodedPassword))
            return null;
        Member member = new Member();
        member.setName(name.trim());
        member.setPhone(phone.trim());
        member.setPassword(encodedPassword);
        return member;
    }
}
