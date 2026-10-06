package com.vanxx.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vanxx.library.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
}