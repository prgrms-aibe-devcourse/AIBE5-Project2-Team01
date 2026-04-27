package com.example.meetball.domain.people.dto;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.entity.ProfileTechStack;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PeopleProfileResponse {

    private Long profileId;
    private String nickname;
    private String position;
    private String techStack;
    private boolean isPublic;
    private double meetBallIndex;

    public static PeopleProfileResponse from(Profile profile, double meetBallIndex) {
        return PeopleProfileResponse.builder()
                .profileId(profile.getId())
                .nickname(profile.getNickname())
                .position(profile.getPosition())
                .techStack(formatTechStacks(profile))
                .isPublic(profile.isPublic())
                .meetBallIndex(meetBallIndex)
                .build();
    }

    private static String formatTechStacks(Profile profile) {
        if (profile.getTechStackSelections() != null && !profile.getTechStackSelections().isEmpty()) {
            return profile.getTechStackSelections().stream()
                    .map(ProfileTechStack::getTechStackName)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return profile.getTechStack();
    }
}
