package com.example.meetball.domain.people.dto;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PeopleProjectResponse {

    private Long projectId;
    private String title;
    private String userRole;
    private String status;
    private boolean closed;
    private boolean completed;

    @JsonProperty("dDay")
    private Long dDay;

    public static PeopleProjectResponse from(ParticipatedProjectResponse project) {
        return PeopleProjectResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .userRole(project.getUserRole())
                .status(project.getStatus())
                .closed(project.isClosed())
                .completed(project.isCompleted())
                .dDay(project.getDDay())
                .build();
    }
}
