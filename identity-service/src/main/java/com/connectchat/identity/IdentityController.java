package com.connectchat.identity;

import com.connectchat.identity.util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class IdentityController {

    @GetMapping("")
    public ResponseEntity<Void> test() {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/register")
    public ResponseEntity<Response<Void>> register() {
        return ResponseEntity.ok(null);
    }
}
