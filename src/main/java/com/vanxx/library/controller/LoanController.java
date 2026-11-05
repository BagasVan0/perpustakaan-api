package com.vanxx.library.controller;

import com.vanxx.library.dto.LoanRequest;
import com.vanxx.library.dto.LoanResponse;
import com.vanxx.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Loans", description = "Borrowing and Returning books (JWT token's needed)")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    @Operation(summary = "Take All loans (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Loan list succesfully retrieved")
    @ApiResponse(responseCode = "401", description = "Token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "Not an ADMIN")
    public List<LoanResponse> getAll() {
        return loanService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Borrowing books", description = "MEMBERs can borrow books on their own behalf (memberId is extracted from the token). ADMINs are allowed to loan books on behalf of any member by providing the memberId.")
    @ApiResponse(responseCode = "201", description = "Loan created successfully")
    @ApiResponse(responseCode = "400", description = "Book or member not found")
    @ApiResponse(responseCode = "401", description = "There is no token or token not valid")
    @ApiResponse(responseCode = "403", description = "Account is not linked to member")
    @ApiResponse(responseCode = "409", description = "The requested book is currently borrowed by another member")
    public LoanResponse borrow(@RequestBody LoanRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = isAdmin(jwt) ? request.memberId() : memberIdFrom(jwt);
        return loanService.borrow(new LoanRequest(memberId, request.bookId()));
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "Return book")
    @ApiResponse(responseCode = "200", description = "Book returned successfully")
    @ApiResponse(responseCode = "401", description = "There is no token or token not valid")
    @ApiResponse(responseCode = "403", description = "Unauthorized loan access")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    @ApiResponse(responseCode = "409", description = "Loan is already returned")
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
