package com.example.meetball.domain.mypage.dto;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.entity.ProfileTechStack;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageProfileResponse {
    private Long profileId;
    private String nickname;
    private String email;
    private String position;
    private String techStack;
    private boolean isPublic;
    private double meetBallIndex;
    private boolean isOwner; // 본인 여부 추가

    // 편의용 변환 메서드
    public static MyPageProfileResponse from(Profile profile, double meetBallIndex, boolean isOwner) {
        String displayEmail = profile.getEmail();
        
        // 본인이 아닐 경우 이메일 마스킹 (예: abc***@test.com)
        if (!isOwner && displayEmail != null && displayEmail.contains("@")) {
            String[] parts = displayEmail.split("@");
            if (parts[0].length() > 3) {
                displayEmail = parts[0].substring(0, 3) + "***@" + parts[1];
            } else {
                displayEmail = (parts[0].length() > 0 ? parts[0].charAt(0) : "") + "***@" + parts[1];
            }
        }

        return MyPageProfileResponse.builder()
                .profileId(profile.getId())
                .nickname(profile.getNickname())
                .email(displayEmail)
                .position(profile.getPosition())
                .techStack(formatTechStacks(profile))
                .isPublic(profile.isPublic())
                .meetBallIndex(meetBallIndex)
                .isOwner(isOwner)
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
