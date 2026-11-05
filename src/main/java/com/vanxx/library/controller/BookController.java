package com.vanxx.library.controller;

import com.vanxx.library.dto.BookRequest;
import com.vanxx.library.dto.BookResponse;
import com.vanxx.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "See the list of books and add new books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Take all books")
    @ApiResponse(responseCode = "200", description = "Books list succesfully added")
    public List<BookResponse> getAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Take's one book from id")
    @ApiResponse(responseCode = "200", description = "Book found")
    @ApiResponse(responseCode = "404", description = "Books not found")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add new books")
    @ApiResponse(responseCode = "201", description = "Books succesfully created")
    @ApiResponse(responseCode = "400", description = "Data not valid")
    @ApiResponse(responseCode = "404", description = "Category or writer's not found")
    @ApiResponse(responseCode = "409", description = "Conflict on data")
    public BookResponse create(@RequestBody BookRequest request) {
        return bookService.create(request);
    }
}
