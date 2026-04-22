package com.example.meetball.domain.user.service;

import com.example.meetball.domain.user.dto.UserProfileUpdateRequest;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Collections;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // TODO: 현재는 임시로 이메일(또는 ID)을 통해 유저를 찾는 방식으로 구현.
    // 추후 Spring Security의 @AuthenticationPrincipal과 연동 예정
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

        user.updateProfile(
                request.getNickname(),
                request.getJobTitle(),
                request.getTechStack(),
                request.isPublic()
        );
    }

    @Value("${google.client.id:}")
    private String googleClientId;

    @Transactional
    public User processGoogleLogin(String credential) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                return userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(name != null ? name : "User_" + System.currentTimeMillis())
                            .role("MEMBER")
                            .jobTitle("-")
                            .techStack("-")
                            .isPublic(true)
                            .build();
                    return userRepository.save(newUser);
                });
            } else {
                throw new IllegalArgumentException("Invalid ID token.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google token: " + e.getMessage());
        }
    }
}
