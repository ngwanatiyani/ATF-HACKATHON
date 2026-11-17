package com.example.atfhackathon2026.controller;

import com.example.atfhackathon2026.request.BriefRequest;
import com.example.atfhackathon2026.response.BriefResponse;
import com.example.atfhackathon2026.service.BriefService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class BriefController {
    private final BriefService briefs;

    public BriefController(BriefService briefs) {
        this.briefs = briefs;
    }

    @PostMapping("/brief")
    public BriefResponse brief(@RequestBody BriefRequest request) {
        try {
            return briefs.check(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, ex.getMessage());
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Exa could not run the road check");
        }
    }
}
