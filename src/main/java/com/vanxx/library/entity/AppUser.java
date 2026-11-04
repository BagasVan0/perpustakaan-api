package com.vanxx.library.entity;

import jakarta.persistence.*;

@Entity 
@Table(name = "app_users")
public class AppUser {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    @Column(name = "member_id")
    private Long memberId;

    protected AppUser() {}

    public AppUser(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public AppUser(String username, String passwordHash, String role, Long memberId) {
        this(username, passwordHash, role);
        this.memberId = memberId;
    }

    public Long getMemberId() { return memberId; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}
