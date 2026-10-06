package com.vanxx.library.dto;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        String member,
        String book,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status) {
}
