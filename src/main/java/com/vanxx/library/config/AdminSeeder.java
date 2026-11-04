package com.vanxx.library.config;

import com.vanxx.library.entity.AppUser;
import com.vanxx.library.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component 
public class AdminSeeder implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminSeeder(AppUserRepository users, PasswordEncoder passwordEncoder, @Value("${app.admin.username}") String username, @Value("${app.admin.password}") String password) {
        this.users = users;
        this.username = username;
        this.password = password;
        this.passwordEncoder = passwordEncoder;
    }

    @Override 
    public void run(String... args) {
        if (password.isBlank()) {
            log.warn("ADMIN_PASSWORD not set yet, admin account will not be created");
            return;
        }
        if (!users.existsByUsername(username)) {
            users.save(new AppUser(username, passwordEncoder.encode(password), "ADMIN"));
            log.info("Admin account created '{}'", username);
        }
    }
}
