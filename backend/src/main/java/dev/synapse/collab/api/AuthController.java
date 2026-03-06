package dev.synapse.collab.api;

import dev.synapse.collab.repo.UserRepository;
import dev.synapse.collab.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final CurrentUser currentUser;
    private final UserRepository users;

    public AuthController(AuthService auth, CurrentUser currentUser, UserRepository users) {
        this.auth = auth;
        this.currentUser = currentUser;
        this.users = users;
    }

    @PostMapping("/register")
    public ApiDtos.AuthResponse register(@Valid @RequestBody ApiDtos.RegisterRequest request) {
        return auth.register(request);
    }

    @PostMapping("/login")
    public ApiDtos.AuthResponse login(@Valid @RequestBody ApiDtos.LoginRequest request) {
        return auth.login(request);
    }

    @GetMapping("/me")
    public ApiDtos.UserView me() {
        var user = users.findById(currentUser.id()).orElseThrow();
        return new ApiDtos.UserView(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
