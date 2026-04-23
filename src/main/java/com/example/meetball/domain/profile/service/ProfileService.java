package com.example.meetball.domain.profile.service;

import com.example.meetball.domain.account.entity.Account;
import com.example.meetball.domain.account.repository.AccountRepository;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.profile.dto.ProfileOnboardingRequest;
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
import java.util.Set;
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

    @Transactional
    public Profile completeOnboarding(Long profileId, ProfileOnboardingRequest request) {
        Profile profile = getProfileById(profileId);

        String normalizedName = requireText(request.getName(), "이름을 입력해주세요.", 30);
        String normalizedPhoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        if (request.getBirthDate() == null) {
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        }
        if (request.getBirthDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("생년월일은 오늘 이후일 수 없습니다.");
        }

        String normalizedGender = normalizeGender(request.getGender());
        String normalizedPosition = ProjectSelectionCatalog.normalizeSinglePositionName(request.getJobTitle());
        String normalizedExperienceYears = requireText(request.getExperienceYears(), "경력을 선택해주세요.", 255);
        String normalizedOrganization = requireText(request.getOrganization(), "소속을 입력해주세요.", 100);
        List<String> normalizedTechStacks = ProjectSelectionCatalog.normalizeTechStackNames(request.getTechStacks());
        if (normalizedTechStacks.isEmpty()) {
            throw new IllegalArgumentException("기술 스택을 1개 이상 선택해주세요.");
        }

        Position position = positionRepository.findByName(normalizedPosition)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 포지션입니다: " + normalizedPosition));
        List<TechStack> techStacks = normalizedTechStacks.stream()
                .map(techStack -> techStackRepository.findByName(techStack)
                        .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 기술스택입니다: " + techStack)))
                .toList();

        profile.completeOnboarding(
                normalizedName,
                normalizedPhoneNumber,
                request.getBirthDate(),
                normalizedGender,
                position,
                normalizedExperienceYears,
                normalizedOrganization,
                request.isOrgVisible(),
                techStacks
        );
        return profile;
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

    private String requireText(String value, String message, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String normalizeGender(String gender) {
        String normalizedGender = requireText(gender, "성별을 선택해주세요.", 10);
        return switch (normalizedGender) {
            case "남", "남성", "남자", "male", "Male", "M", "m" -> "남자";
            case "여", "여성", "여자", "female", "Female", "F", "f" -> "여자";
            default -> throw new IllegalArgumentException("지원하지 않는 성별 값입니다.");
        };
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String digits = requireText(phoneNumber, "전화번호를 입력해주세요.", 30).replaceAll("\\D", "");
        if (digits.length() < 9 || digits.length() > 11) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
        }
        if (digits.startsWith("02")) {
            if (digits.length() == 9) {
                return digits.replaceFirst("(02)(\\d{3})(\\d{4})", "$1-$2-$3");
            }
            if (digits.length() == 10) {
                return digits.replaceFirst("(02)(\\d{4})(\\d{4})", "$1-$2-$3");
            }
        }
        if (digits.length() == 10) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }
        return digits;
    }
}
