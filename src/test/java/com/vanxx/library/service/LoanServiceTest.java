package com.vanxx.library.service;

import com.vanxx.library.dto.LoanRequest;
import com.vanxx.library.dto.LoanResponse;
import com.vanxx.library.entity.Book;
import com.vanxx.library.entity.Loan;
import com.vanxx.library.entity.Member;
import com.vanxx.library.repository.BookRepository;
import com.vanxx.library.repository.LoanRepository;
import com.vanxx.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LoanService loanService;

    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setName("Sari Dewi");
        ReflectionTestUtils.setField(member, "id", 2L);

        book = new Book();
        book.setTitle("Clean Code");
        ReflectionTestUtils.setField(book, "id", 5L);
    }

    private Loan newLoan(LocalDate dueDate, LocalDate returnDate) {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now().minusDays(20));
        loan.setDueDate(dueDate);
        loan.setReturnDate(returnDate);
        return loan;
    }

    private void assertStatus(HttpStatus expected, Runnable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(expected, ex.getStatusCode());
    }

    @Test
    void borrow_success_createsLoanDue14Days() {
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByBookIdAndReturnDateIsNull(5L)).thenReturn(false);
        when(loanRepository.saveAndFlush(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse result = loanService.borrow(new LoanRequest(2L, 5L));

        assertEquals("dipinjam", result.status());
        assertEquals("Sari Dewi", result.member());
        assertEquals("Clean Code", result.book());
        assertEquals(LocalDate.now().plusDays(14), result.dueDate());
        assertNull(result.returnDate());
    }

    @Test
    void borrow_bookAlreadyBorrowed_throws409() {
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByBookIdAndReturnDateIsNull(5L)).thenReturn(true);

        assertStatus(HttpStatus.CONFLICT, () -> loanService.borrow(new LoanRequest(2L, 5L)));
        verify(loanRepository, never()).saveAndFlush(any());
    }

    @Test
    void borrow_memberNotFound_throws400() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertStatus(HttpStatus.BAD_REQUEST, () -> loanService.borrow(new LoanRequest(99L, 5L)));
    }

    @Test
    void borrow_bookNotFound_throws400() {
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertStatus(HttpStatus.BAD_REQUEST, () -> loanService.borrow(new LoanRequest(2L, 99L)));
    }

    @Test
    void borrow_missingIds_throws400() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> loanService.borrow(new LoanRequest(null, 5L)));
    }

    @Test
    void giveBack_success_setsReturnDate() {
        Loan loan = newLoan(LocalDate.now().plusDays(5), null);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        LoanResponse result = loanService.giveBack(1L);

        assertEquals("kembali", result.status());
        assertEquals(LocalDate.now(), result.returnDate());
    }

    @Test
    void giveBack_alreadyReturned_throws409() {
        Loan loan = newLoan(LocalDate.now().plusDays(5), LocalDate.now().minusDays(1));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertStatus(HttpStatus.CONFLICT, () -> loanService.giveBack(1L));
    }

    @Test
    void giveBack_loanNotFound_throws404() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertStatus(HttpStatus.NOT_FOUND, () -> loanService.giveBack(999L));
    }

    @Test
    void findAll_overdueLoan_hasStatusTelat() {
        Loan overdue = newLoan(LocalDate.now().minusDays(6), null);
        when(loanRepository.findAll()).thenReturn(List.of(overdue));

        List<LoanResponse> result = loanService.findAll();

        assertEquals(1, result.size());
        assertEquals("TELAT", result.get(0).status());
    }
}
