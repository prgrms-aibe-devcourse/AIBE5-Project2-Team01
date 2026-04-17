package com.example.meetball.domain.project.dto;

public class ProjectListResponseDto {

    private Long id;
    private String name;

    public ProjectListResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
