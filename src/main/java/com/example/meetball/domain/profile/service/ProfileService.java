package com.example.meetball.domain.profile.service;

import com.example.meetball.domain.account.entity.Account;
import com.example.meetball.domain.account.repository.AccountRepository;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.profile.dto.ProfileUpdateRequest;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
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
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final PositionRepository positionRepository;
    private final TechStackRepository techStackRepository;

    // 현재 컨트롤러 계약은 HttpSession의 profileId를 기준으로 프로필을 조회합니다.
    @Transactional(readOnly = true)
    public Profile getProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findDefaultProfile() {
        return profileRepository.findFirstByOrderByIdAsc();
    }
    @Transactional
    public void updateProfile(Long profileId, ProfileUpdateRequest request) {
        Profile profile = getProfileById(profileId);
        
        // 닉네임 중복 체크 (기존 닉네임과 다를 경우에만)
        if (!profile.getNickname().equals(request.getNickname()) &&
            profileRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        String normalizedPosition = ProjectSelectionCatalog.normalizeSinglePositionName(request.getJobTitle());
        List<String> normalizedTechStacks = ProjectSelectionCatalog.normalizeTechStackNames(request.getTechStacks());
        Position position = positionRepository.findByName(normalizedPosition)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 포지션입니다: " + normalizedPosition));
        List<TechStack> techStacks = normalizedTechStacks.stream()
                .map(techStack -> techStackRepository.findByName(techStack)
                        .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 기술스택입니다: " + techStack)))
                .toList();
        profile.updateProfile(
                request.getNickname(),
                position,
                techStacks,
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
                String socialIdentifier = payload.getSubject();

                Optional<Profile> existingProfile = profileRepository
                        .findFirstByAccountSocialProviderAndAccountSocialIdentifierOrderByDefaultProfileDescIdAsc(
                                Account.PROVIDER_GOOGLE,
                                socialIdentifier
                        )
                        .or(() -> profileRepository.findByEmail(email));
                if (existingProfile.isPresent()) {
                    Profile profile = existingProfile.get();
                    if (profile.getAccount() != null) {
                        profile.getAccount().updateGoogleIdentity(email, socialIdentifier, name);
                    }
                    return new GoogleLoginResult(profile, !profile.isProfileComplete());
                }

                Account account = accountRepository.findByEmail(email)
                        .orElseGet(() -> accountRepository.save(Account.google(email, socialIdentifier, name)));
                Profile newProfile = Profile.defaultProfile(account, name != null ? name : emailPrefix(email), true);
                return new GoogleLoginResult(profileRepository.save(newProfile), true);
            } else {
                throw new IllegalArgumentException("Invalid ID token.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google token: " + e.getMessage());
        }
    }

    public record GoogleLoginResult(Profile profile, boolean newlyCreated) {
    }

    private String emailPrefix(String email) {
        if (email == null || email.isBlank()) {
            return "Meetball_" + System.currentTimeMillis();
        }
        int atIndex = email.indexOf('@');
        String prefix = atIndex > 0 ? email.substring(0, atIndex) : email;
        return prefix.isBlank() ? "Meetball_" + System.currentTimeMillis() : prefix;
    }
}
