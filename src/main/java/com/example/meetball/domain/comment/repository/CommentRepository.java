package com.example.meetball.domain.comment.repository;

import com.example.meetball.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 부모 댓글(parent == null)인 것만 페이징 처리하여 최신순으로 가져오기
    Page<Comment> findByProjectIdAndParentIsNullOrderByCreatedAtDesc(Long projectId, Pageable pageable);
    
    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
