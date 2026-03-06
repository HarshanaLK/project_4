package dev.synapse.collab.service;

import dev.synapse.collab.api.*;
import dev.synapse.collab.domain.User;
import dev.synapse.collab.repo.UserRepository;
import dev.synapse.collab.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @Transactional
    public ApiDtos.AuthResponse register(ApiDtos.RegisterRequest request) {
        if (users.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "An account already exists for this email.");
        }

        User user = users.save(new User(
                request.email().trim(),
                request.displayName().trim(),
                passwords.encode(request.password())));

        return response(user);
    }

    public ApiDtos.AuthResponse login(ApiDtos.LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Invalid email or password."));

        if (!passwords.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Invalid email or password.");
        }
        return response(user);
    }

    private ApiDtos.AuthResponse response(User user) {
        return new ApiDtos.AuthResponse(jwt.issue(user),
                new ApiDtos.UserView(user.getId(), user.getEmail(), user.getDisplayName()));
    }
}
