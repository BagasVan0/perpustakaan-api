package com.vanxx.library.service;

import com.vanxx.library.dto.BookRequest;
import com.vanxx.library.dto.BookResponse;
import com.vanxx.library.entity.Author;
import com.vanxx.library.entity.Book;
import com.vanxx.library.entity.Category;
import com.vanxx.library.repository.AuthorRepository;
import com.vanxx.library.repository.BookRepository;
import com.vanxx.library.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Buku tidak ditemukan: " + id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Judul wajib diisi");
        }
        if (request.categoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId wajib diisi");
        }
        if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ISBN sudah terdaftar: " + request.isbn());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Kategori tidak ditemukan: " + request.categoryId()));

        Book book = new Book();
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPublicationYear(request.publicationYear());
        book.setCategory(category);

        if (request.authorIds() != null && !request.authorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(request.authorIds());
            if (authors.size() != new HashSet<>(request.authorIds()).size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ada authorId yang tidak ditemukan");
            }
            book.getAuthors().addAll(authors);
        }

        return toResponse(bookRepository.save(book));
    }

    private BookResponse toResponse(Book book) {
        List<String> authors = book.getAuthors().stream()
                .map(Author::getName)
                .sorted()
                .toList();
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getCategory().getName(),
                authors);
    }
}
