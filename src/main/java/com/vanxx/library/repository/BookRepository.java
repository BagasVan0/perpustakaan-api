package com.vanxx.library.repository;

import com.vanxx.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);
    
    @Query("""
            select b from Book b
            where lower(b.title) like lower(concat('%', :title, '%'))
              and (:categoryId is null or b.category.id = :categoryId)
            """)
    Page<Book> search(@Param("title") String title, @Param("categoryId") Long categoryId, Pageable pageable);
}
