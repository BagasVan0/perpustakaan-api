package com.vanxx.library.dto;

import java.util.List;

public record BookRequest(
        String title,
        String isbn,
        Integer publicationYear,
        Long categoryId,
        List<Long> authorIds) {
}
