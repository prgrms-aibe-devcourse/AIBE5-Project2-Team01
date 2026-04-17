package com.example.meetball.domain.application.dto;

public class ApplicationRequestDto {
    private String applicantName;
    private String position;
    private String message;

    public ApplicationRequestDto() {}

    public ApplicationRequestDto(String applicantName, String position, String message) {
        this.applicantName = applicantName;
        this.position = position;
        this.message = message;
    }

    public String getApplicantName() { return applicantName; }
    public String getPosition() { return position; }
    public String getMessage() { return message; }
}
