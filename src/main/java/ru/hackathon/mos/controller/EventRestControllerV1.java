package ru.hackathon.mos.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventRestControllerV1 {

    @GetMapping("/my-resource")
    public ResponseEntity<String> getMyResource(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // sub
        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(email + ":" + userId);
    }
}
