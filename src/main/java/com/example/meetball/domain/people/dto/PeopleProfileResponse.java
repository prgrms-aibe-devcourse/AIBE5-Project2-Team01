package com.example.meetball.domain.people.dto;

import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.entity.UserTechStack;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PeopleProfileResponse {

    private Long userId;
    private String nickname;
    private String jobTitle;
    private String techStack;
    private boolean isPublic;
    private String role;
    private double meetBallIndex;

    public static PeopleProfileResponse from(User user, double meetBallIndex) {
        return PeopleProfileResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .jobTitle(user.getJobTitle())
                .techStack(formatTechStacks(user))
                .isPublic(user.isPublic())
                .role(user.getRole())
                .meetBallIndex(meetBallIndex)
                .build();
    }

    private static String formatTechStacks(User user) {
        if (user.getTechStackSelections() != null && !user.getTechStackSelections().isEmpty()) {
            return user.getTechStackSelections().stream()
                    .map(UserTechStack::getTechStackName)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return user.getTechStack();
    }
}
