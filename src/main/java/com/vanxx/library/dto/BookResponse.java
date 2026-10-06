package com.vanxx.library.dto;

import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        Integer publicationYear,
        String category,
        List<String> authors) {
}
