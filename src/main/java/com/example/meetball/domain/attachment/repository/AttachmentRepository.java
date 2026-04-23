package com.example.meetball.domain.attachment.repository;

import com.example.meetball.domain.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
