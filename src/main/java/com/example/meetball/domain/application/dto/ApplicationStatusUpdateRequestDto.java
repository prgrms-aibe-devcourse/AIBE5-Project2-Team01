package com.example.meetball.domain.application.dto;

public class ApplicationStatusUpdateRequestDto {
    private String status;

    public ApplicationStatusUpdateRequestDto() {}

    public ApplicationStatusUpdateRequestDto(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
}
