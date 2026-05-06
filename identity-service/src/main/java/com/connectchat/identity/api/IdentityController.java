package com.connectchat.identity.api;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.common.web.Response;
import com.connectchat.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class IdentityController {

    private final AuthService authService;

    @GetMapping("")
    public ResponseEntity<Void> test() {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Response<Void>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.ok(null);
    }
}
