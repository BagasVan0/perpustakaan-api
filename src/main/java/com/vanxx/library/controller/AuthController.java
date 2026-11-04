package com.vanxx.library.controller;


import com.vanxx.library.entity.AppUser;
import com.vanxx.library.entity.Member;
import com.vanxx.library.repository.AppUserRepository;
import com.vanxx.library.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController 
@RequestMapping("/auth")
public class AuthController {
    
    public record RegisterRequest(String username, String password, String name, String email) {}
    public record LoginRequest(String username, String password) {}
    public record RegisterResponse(String username, Long memberId) {}
    public record TokenResponse(String token) {}

    private final AppUserRepository users;
    private final MemberRepository members;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthController(AppUserRepository users, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, MemberRepository members) {
        this.users = users;
        this.members = members;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public RegisterResponse register(@RequestBody RegisterRequest req) {
        if (isBlank(req.username()) || isBlank(req.name()) || isBlank(req.email()) || req.password() == null || req.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be provided and password must be at least 8 characters long");
        }
        if (users.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (members.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Member member = new Member();
        member.setName(req.name());
        member.setEmail(req.email());
        members.save(member);

        users.save(new AppUser(req.username(), passwordEncoder.encode(req.password()), "MEMBER", member.getId()));
        return new RegisterResponse(req.username(), member.getId());
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        AppUser user = users.findByUsername(req.username()).filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash())).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS));
        if (user.getMemberId() != null) {
            claims.claim("memberId", user.getMemberId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenResponse(token);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

}
