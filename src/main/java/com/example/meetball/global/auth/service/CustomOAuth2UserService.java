package com.example.meetball.global.auth.service;

import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 구글 로그인 시 제공되는 정보 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // DB 저장 및 업데이트
        User user = saveOrUpdate(email, name);
        
        // 세션에 userId 저장 (기존 도메인 로직들과의 호환성을 위함)
        httpSession.setAttribute("userId", user.getId());
        httpSession.setAttribute("userNickname", user.getNickname());
        
        // 닉네임이 기본값이거나 직무 정보가 없는 경우 프로필 설정 유도
        if ("-".equals(user.getJobTitle()) || user.getNickname().startsWith("User_")) {
            httpSession.setAttribute("needsProfile", true);
        } else {
            httpSession.removeAttribute("needsProfile");
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes,
                userNameAttributeName
        );
    }

    private User saveOrUpdate(String email, String name) {
        return userRepository.findByEmail(email)
                .map(entity -> {
                    // 이미 존재하는 유저는 그대로 반환 (필요 시 업데이트 로직 추가 가능)
                    return entity;
                })
                .orElseGet(() -> {
                    // 신규 유저 가입
                    User newUser = User.builder()
                            .email(email)
                            .nickname(name != null ? name : "User_" + System.currentTimeMillis())
                            .role("MEMBER") // 기본 권한 MEMBER
                            .jobTitle("-")
                            .techStack("-")
                            .isPublic(true)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
