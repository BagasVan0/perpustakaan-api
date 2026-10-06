package com.vanxx.library.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "books")
public class Book {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String title;

    @Column (unique = true)
    private String isbn;

    private Integer publicationYear;

    @ManyToOne (optional = false)
    @JoinColumn (name = "category_id")
    private Category category;

    @ManyToMany 
    @JoinTable (
        name = "book_authors",
        joinColumns = @JoinColumn (name = "book_id"),
        inverseJoinColumns = @JoinColumn (name = "author_id")
    )

    private Set<Author> authors = new HashSet<>();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear (Integer publicationYear) { this.publicationYear = publicationYear; }
    public Category getCategory() { return category; }
    public void setCategory (Category category) { this.category = category; }
    public Set<Author> getAuthors() { return authors; }
}