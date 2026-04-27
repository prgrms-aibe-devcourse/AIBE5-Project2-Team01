package com.example.meetball.domain.mypage.dto;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.entity.ProfileTechStack;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MyPageProfileResponse {
    private Long profileId;
    private String accountName;
    private String socialProvider;
    private String profileImage;
    private String nickname;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;
    private String position;
    private String experienceYears;
    private String organization;
    private boolean orgVisible;
    private String techStack;
    private String bio;
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
                .accountName(profile.getEditableAccountName())
                .socialProvider(profile.getAccount() != null ? profile.getAccount().getSocialProvider() : null)
                .profileImage(profile.getProfileImage())
                .nickname(profile.getNickname())
                .email(displayEmail)
                .phoneNumber(profile.getPhoneNumber())
                .birthDate(profile.getBirthDate())
                .gender(profile.getGender())
                .position(profile.getPosition())
                .experienceYears(profile.getExperienceYears())
                .organization(profile.getOrganization())
                .orgVisible(profile.isOrgVisible())
                .techStack(formatTechStacks(profile))
                .bio(profile.getBio())
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
