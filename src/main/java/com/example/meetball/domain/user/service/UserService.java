package com.example.meetball.domain.user.service;

import com.example.meetball.domain.user.dto.UserProfileUpdateRequest;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 현재 컨트롤러 계약은 HttpSession의 userId를 기준으로 사용자를 조회합니다.
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Optional<User> findDefaultUser() {
        return userRepository.findFirstByOrderByIdAsc();
    }
    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getUserById(userId);
        
        // 닉네임 중복 체크 (기존 닉네임과 다를 경우에만)
        if (!user.getNickname().equals(request.getNickname()) && 
            userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        String normalizedPosition = ProjectSelectionCatalog.normalizeSinglePositionName(request.getJobTitle());
        List<String> normalizedTechStacks = ProjectSelectionCatalog.normalizeTechStackNames(request.getTechStacks());
        user.updateProfile(
                request.getNickname(),
                normalizedPosition,
                normalizedTechStacks,
                request.isPublic()
        );
    }

    @Value("${google.client.id:}")
    private String googleClientId;

    @Transactional
    public GoogleLoginResult processGoogleLogin(String credential) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    return new GoogleLoginResult(existingUser.get(), false);
                }

                User newUser = User.builder()
                        .email(email)
                        .nickname(name != null ? name : "User_" + System.currentTimeMillis())
                        .role("MEMBER")
                        .jobTitle("")
                        .techStacks(List.of())
                        .isPublic(true)
                        .build();
                return new GoogleLoginResult(userRepository.save(newUser), true);
            } else {
                throw new IllegalArgumentException("Invalid ID token.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google token: " + e.getMessage());
        }
    }

    public record GoogleLoginResult(User user, boolean newlyCreated) {
    }
}
