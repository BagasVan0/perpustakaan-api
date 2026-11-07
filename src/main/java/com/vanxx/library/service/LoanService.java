package com.vanxx.library.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vanxx.library.dto.LoanRequest;
import com.vanxx.library.dto.LoanResponse;
import com.vanxx.library.entity.Book;
import com.vanxx.library.entity.Loan;
import com.vanxx.library.entity.Member;
import com.vanxx.library.repository.BookRepository;
import com.vanxx.library.repository.LoanRepository;
import com.vanxx.library.repository.MemberRepository;

@Service
public class LoanService {

    private static final int LOAN_DAYS = 14;

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository,
                       MemberRepository memberRepository,
                       BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findAll() {
        return loanRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public LoanResponse borrow(LoanRequest request) {
        if (request.memberId() == null || request.bookId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "memberId and bookId are required");
        }

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Member not found: " + request.memberId()));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Book not found: " + request.bookId()));

        if (loanRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Buku is already on loan: " + book.getTitle());
        }

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_DAYS));

        try {
            return toResponse(loanRepository.saveAndFlush(loan));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Book is already on loan: " + book.getTitle());
        }
    }

    @Transactional 
    public LoanResponse giveBack(Long id) {
        return giveBack(id, null);
    }

    @Transactional
    public LoanResponse giveBack(Long id, Long requesterMemberId) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Loan not found: " + id));

        if (requesterMemberId != null && !loan.getMember().getId().equals(requesterMemberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pinjaman ini bukan atas nama Anda");
        }

        if (loan.getReturnDate() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Buku has already been returned");
        }

        loan.setReturnDate(LocalDate.now());
        return toResponse(loan);
    }

    private LoanResponse toResponse(Loan loan) {
        String status;
        if (loan.getReturnDate() != null) {
            status = "Returned";
        } else if (loan.getDueDate().isBefore(LocalDate.now())) {
            status = "OVERDUE";
        } else {
            status = "On loan";
        }
        return new LoanResponse(
                loan.getId(),
                loan.getMember().getName(),
                loan.getBook().getTitle(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                status);
    }
}
