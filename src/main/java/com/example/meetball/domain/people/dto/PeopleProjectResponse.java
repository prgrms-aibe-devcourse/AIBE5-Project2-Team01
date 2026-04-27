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
    private String participantRole;
    private String status;
    private String recruitStatus;
    private String progressStatus;

    @JsonProperty("dDay")
    private Long dDay;

    public static PeopleProjectResponse from(ParticipatedProjectResponse project) {
        return PeopleProjectResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .participantRole(project.getParticipantRole())
                .status(project.getStatus())
                .recruitStatus(project.getRecruitStatus())
                .progressStatus(project.getProgressStatus())
                .dDay(project.getDDay())
                .build();
    }
}
