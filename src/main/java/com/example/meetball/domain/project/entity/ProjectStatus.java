package com.example.meetball.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;

public enum ProjectStatus {
    RECRUITING, // 모집 중
    PROCEEDING, // 진행 중
    COMPLETED   // 종료됨
}
