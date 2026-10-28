package com.quizapp.controller;

import com.quizapp.dto.Dtos.*;
import com.quizapp.model.User;
import com.quizapp.repository.UserRepository;
import com.quizapp.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
        if (r.name() == null || r.name().isBlank() || r.email() == null || r.email().isBlank()
                || r.password() == null || r.password().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name, email and a password of at least 6 characters are required"));
        }
        String email = r.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "This email is already registered"));
        }
        User user = new User();
        user.setName(r.name().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(r.password()));   // hash the password
        user.setRole("USER");
        userRepo.save(user);

        return ResponseEntity.ok(new AuthResponse(jwtUtil.generate(email, "USER"), user.getName(), "USER"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest r) {
        if (r.email() == null || r.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        Optional<User> found = userRepo.findByEmail(r.email().trim().toLowerCase());
        if (found.isEmpty() || !encoder.matches(r.password(), found.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }
        User user = found.get();
        return ResponseEntity.ok(new AuthResponse(jwtUtil.generate(user.getEmail(), user.getRole()),
                user.getName(), user.getRole()));
    }
}
