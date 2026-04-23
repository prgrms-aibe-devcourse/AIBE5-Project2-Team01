package com.example.meetball.domain.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String nickname;
    private String jobTitle;
    private List<String> techStacks = List.of();
    @JsonProperty("isPublic")
    private boolean isPublic;

    public List<String> getTechStacks() {
        return techStacks == null ? List.of() : techStacks;
    }
}
