package com.example.meetball.domain.auth.dto;

import com.example.meetball.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String role;

    public AuthResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
    }
}
