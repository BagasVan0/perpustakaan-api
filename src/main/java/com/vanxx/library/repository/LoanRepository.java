package com.vanxx.library.repository;

import com.vanxx.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
}
