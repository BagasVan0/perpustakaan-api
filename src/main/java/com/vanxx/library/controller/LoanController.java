package com.vanxx.library.controller;

import com.vanxx.library.dto.LoanRequest;
import com.vanxx.library.dto.LoanResponse;
import com.vanxx.library.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanResponse> getAll() {
        return loanService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse borrow(@RequestBody LoanRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = isAdmin(jwt) ? request.memberId() : memberIdFrom(jwt);
        return loanService.borrow(new LoanRequest(memberId, request.bookId()));
    }

    @PutMapping("/{id}/return")
    public LoanResponse giveBack(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (isAdmin(jwt)) {
            return loanService.giveBack(id);
        }
        return loanService.giveBack(id, memberIdFrom(jwt));
    }

    private static boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("role"));
    }

    private static Long memberIdFrom(Jwt jwt) {
        Number claim = jwt.getClaim("memberId");
        if (claim == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akun ini belum terhubung ke data Member");
        }
        return claim.longValue();
    }
}
