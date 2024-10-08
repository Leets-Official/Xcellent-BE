package com.leets.xcellentbe.domain.comment.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leets.xcellentbe.domain.comment.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
