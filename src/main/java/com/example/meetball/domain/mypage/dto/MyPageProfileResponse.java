package com.example.meetball.domain.mypage.dto;

import com.example.meetball.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageProfileResponse {
    private Long userId;
    private String nickname;
    private String email;
    private String jobTitle;
    private String techStack;
    private boolean isPublic;
    private String role;
    private double meetBallIndex;
    private boolean isOwner; // 본인 여부 추가

    // 편의용 변환 메서드
    public static MyPageProfileResponse from(User user, double meetBallIndex, boolean isOwner) {
        String displayEmail = user.getEmail();
        
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
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(displayEmail)
                .jobTitle(user.getJobTitle())
                .techStack(user.getTechStack())
                .isPublic(user.isPublic())
                .role(user.getRole())
                .meetBallIndex(meetBallIndex)
                .isOwner(isOwner)
                .build();
    }
}
