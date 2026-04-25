package com.example.meetball.domain.profile.entity;

import com.example.meetball.domain.account.entity.Account;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    public static final String PROFILE_STATUS_PUBLIC = "PUBLIC";
    public static final String PROFILE_STATUS_PRIVATE = "PRIVATE";
    public static final String PROFILE_STATUS_INCOMPLETE = "INCOMPLETE";
    private static final String TEMP_NICKNAME_PREFIX = "__temp__";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "is_default_profile", nullable = false)
    private boolean defaultProfile;

    @Column(length = 100)
    private String organization;

    @Column(name = "is_org_visible", nullable = false)
    private boolean orgVisible;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "status_message", length = 100)
    private String statusMessage;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_status", nullable = false, length = 20)
    private String profileStatus = PROFILE_STATUS_INCOMPLETE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ProfileTechStack> techStackSelections = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ProfilePosition> positionSelections = new ArrayList<>();

    @Builder
    public Profile(String email, String nickname, boolean isPublic) {
        this(Account.google(email, buildSeedSocialIdentifier(email, nickname)), sanitizeProvidedNickname(nickname), isPublic, true);
    }

    private Profile(Account account, String nickname, boolean isPublic, boolean defaultProfile) {
        this.account = account;
        this.nickname = nickname;
        this.defaultProfile = defaultProfile;
        this.profileStatus = isPublic ? PROFILE_STATUS_PUBLIC : PROFILE_STATUS_PRIVATE;
        this.createdAt = LocalDateTime.now();
    }

    private static String buildSeedSocialIdentifier(String email, String nickname) {
        if (email != null && !email.isBlank()) {
            return "seed:" + email;
        }
        return "seed:" + sanitizeProvidedNickname(nickname);
    }

    public static Profile defaultProfile(Account account, String nickname, boolean needsProfileCompletion) {
        String resolvedNickname = (nickname == null || nickname.isBlank())
                ? createTemporaryNickname(account != null ? account.getSocialIdentifier() : null)
                : sanitizeProvidedNickname(nickname);
        Profile profile = new Profile(account, resolvedNickname, !needsProfileCompletion, true);
        if (needsProfileCompletion) {
            profile.profileStatus = PROFILE_STATUS_INCOMPLETE;
        }
        return profile;
    }

    public void updateProfile(
            String nickname,
            String organization,
            boolean orgVisible,
            String bio,
            Position position,
            String experienceYears,
            List<TechStack> techStacks,
            boolean isPublic
    ) {
        this.nickname = sanitizeProvidedNickname(nickname);
        this.organization = sanitizeText(organization, 100);
        this.orgVisible = orgVisible;
        this.bio = sanitizeText(bio, 1000);
        this.profileStatus = isPublic ? PROFILE_STATUS_PUBLIC : PROFILE_STATUS_PRIVATE;
        this.updatedAt = LocalDateTime.now();
        replacePosition(position, experienceYears);
        replaceTechStacks(techStacks);
    }

    public void replacePosition(Position position) {
        replacePosition(position, null);
    }

    public void replacePosition(Position position, String experienceYears) {
        if (position == null) {
            this.positionSelections.clear();
            return;
        }

        this.positionSelections.removeIf(existing -> !Objects.equals(position.getId(), existing.getPositionId()));
        ProfilePosition selectedPosition = this.positionSelections.stream()
                .filter(existing -> Objects.equals(position.getId(), existing.getPositionId()))
                .findFirst()
                .orElse(null);
        if (selectedPosition == null) {
            this.positionSelections.add(new ProfilePosition(this, position, experienceYears));
            return;
        }
        selectedPosition.updateExperienceYears(experienceYears);
    }

    public void replaceTechStacks(List<TechStack> techStacks) {
        List<TechStack> requestedTechStacks = normalizeTechStacks(techStacks);
        this.techStackSelections.removeIf(existing -> requestedTechStacks.stream()
                .noneMatch(techStack -> Objects.equals(techStack.getId(), existing.getTechStackId())));

        for (TechStack techStack : requestedTechStacks) {
            boolean alreadySelected = this.techStackSelections.stream()
                    .anyMatch(existing -> Objects.equals(techStack.getId(), existing.getTechStackId()));
            if (!alreadySelected) {
                this.techStackSelections.add(new ProfileTechStack(this, techStack));
            }
        }
    }

    private List<TechStack> normalizeTechStacks(List<TechStack> techStacks) {
        if (techStacks == null) {
            return List.of();
        }
        List<Long> seenIds = new ArrayList<>();
        List<TechStack> normalized = new ArrayList<>();
        for (TechStack techStack : techStacks) {
            if (techStack == null || techStack.getId() == null || seenIds.contains(techStack.getId())) {
                continue;
            }
            seenIds.add(techStack.getId());
            normalized.add(techStack);
        }
        return normalized;
    }

    public String getEmail() {
        return account != null ? account.getEmail() : "";
    }

    public String getPhoneNumber() {
        return account != null ? account.getPhoneNumber() : "";
    }

    public String getAccountName() {
        if (account == null) {
            return nickname;
        }
        String accountName = account.getName();
        return accountName != null && !accountName.isBlank() ? accountName : nickname;
    }

    public String getEditableAccountName() {
        if (account == null || account.getName() == null) {
            return "";
        }
        return account.getName();
    }

    public LocalDate getBirthDate() {
        return account != null ? account.getBirthDate() : null;
    }

    public String getGender() {
        return account != null ? account.getGender() : "";
    }

    public String getPosition() {
        return positionSelections.stream()
                .map(ProfilePosition::getPositionName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    public String getExperienceYears() {
        return positionSelections.stream()
                .map(ProfilePosition::getExperienceYears)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse("");
    }

    public List<String> getTechStackNames() {
        return techStackSelections.stream()
                .map(ProfileTechStack::getTechStackName)
                .filter(Objects::nonNull)
                .toList();
    }

    public String getTechStack() {
        return techStackSelections.stream()
                .map(ProfileTechStack::getTechStackName)
                .collect(Collectors.joining(", "));
    }

    public boolean isPublic() {
        return !PROFILE_STATUS_PRIVATE.equals(profileStatus);
    }

    public boolean isProfileComplete() {
        return !PROFILE_STATUS_INCOMPLETE.equals(profileStatus)
                && nickname != null
                && !nickname.isBlank()
                && account != null
                && account.getName() != null
                && !account.getName().isBlank()
                && account.getPhoneNumber() != null
                && !account.getPhoneNumber().isBlank()
                && account.getBirthDate() != null
                && account.getGender() != null
                && !account.getGender().isBlank()
                && getExperienceYears() != null
                && !getExperienceYears().isBlank()
                && !positionSelections.isEmpty()
                && !techStackSelections.isEmpty();
    }

    public boolean isTemporaryNickname() {
        return nickname != null && nickname.startsWith(TEMP_NICKNAME_PREFIX);
    }

    public Long getAccountId() {
        return account != null ? account.getId() : null;
    }

    public void changeRole(String role) {
        this.updatedAt = LocalDateTime.now();
    }

    public void completeOnboarding(
            String name,
            String nickname,
            String phoneNumber,
            LocalDate birthDate,
            String gender,
            Position position,
            String experienceYears,
            String organization,
            boolean orgVisible,
            List<TechStack> techStacks
    ) {
        if (account != null) {
            account.updateBasicInfo(name, phoneNumber, birthDate, gender);
        }
        this.nickname = sanitizeProvidedNickname(nickname);
        this.organization = sanitizeText(organization, 100);
        this.orgVisible = orgVisible;
        this.updatedAt = LocalDateTime.now();
        replacePosition(position, experienceYears);
        replaceTechStacks(techStacks);
        this.profileStatus = PROFILE_STATUS_PUBLIC;
    }

    private static String sanitizeProvidedNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return createTemporaryNickname(null);
        }
        String trimmed = nickname.trim();
        return trimmed.length() <= 30 ? trimmed : trimmed.substring(0, 30);
    }

    private static String createTemporaryNickname(String seed) {
        String normalizedSeed = seed == null ? "" : seed.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        if (normalizedSeed.isBlank()) {
            normalizedSeed = Long.toString(System.currentTimeMillis(), 36);
        }
        String candidate = TEMP_NICKNAME_PREFIX + normalizedSeed;
        return candidate.length() <= 30 ? candidate : candidate.substring(0, 30);
    }

    private static String sanitizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
