package com.vanxx.library.controller;

import com.vanxx.library.dto.BookRequest;
import com.vanxx.library.dto.BookResponse;
import com.vanxx.library.dto.PageResponse;
import com.vanxx.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "See the list of books and add new books")
public class BookController {

    private static final Set<String> SORTABLE = Set.of("id", "title", "publicationYear");
    private static final int MAX_SIZE = 100;

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Take all books", description = "All parameters are optional. Without parameters, it returns the first page of all books.")
    @ApiResponse(responseCode = "200", description = "Book page retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid page, size, sortBy, or direction parameter")
    public PageResponse<BookResponse> getAll(
            @Parameter(description = "Partial title search, case-insensitive")
            @RequestParam(required = false) String title,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Page number, starting from 0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of books per page (1-100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by: id, title, or publicationYear")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "asc or desc")
            @RequestParam(defaultValue = "asc") String direction) {

        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be at least 0 and size must be between 1 and 100 " + MAX_SIZE);
        }
        if (!SORTABLE.contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "sortBy must be one of: id, title, publicationYear");
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "direction must be asc or desc");
        }

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        if (!sortBy.equals("id")) {
            sort = sort.and(Sort.by("id"));
        }
        return bookService.search(title, categoryId, PageRequest.of(page, size, sort));
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
