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
    public LoanResponse borrow(@RequestBody LoanRequest request) {
        return loanService.borrow(request);
    }

    @PutMapping("/{id}/return")
    public LoanResponse giveBack(@PathVariable Long id) {
        return loanService.giveBack(id);
    }
}
