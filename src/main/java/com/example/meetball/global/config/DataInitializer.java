package com.example.meetball.global.config;

import com.example.meetball.domain.account.repository.AccountRepository;
import com.example.meetball.domain.bookmarkedproject.entity.BookmarkedProject;
import com.example.meetball.domain.bookmarkedproject.repository.BookmarkedProjectRepository;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.entity.ProjectRecruitPosition;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.projectapplication.entity.ProjectApplicationStatus;
import com.example.meetball.domain.projectapplication.repository.ProjectApplicationRepository;
import com.example.meetball.domain.projectresource.repository.ProjectResourceRepository;
import com.example.meetball.domain.review.entity.PeerReview;
import com.example.meetball.domain.review.entity.ProjectReview;
import com.example.meetball.domain.review.repository.PeerReviewRepository;
import com.example.meetball.domain.review.repository.ProjectReviewRepository;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.viewhistory.entity.ViewHistory;
import com.example.meetball.domain.viewhistory.repository.ViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final int TOTAL_PROFILE_COUNT = 50;
    private static final int TOTAL_PROJECT_COUNT = 35;
    private static final int FIXTURE_PROFILE_COUNT = 4;
    private static final int FIXTURE_PROJECT_COUNT = 3;
    private static final List<Integer> PROJECT_CAPACITY_PLAN = List.of(
            4, 5, 5, 2, 3, 8, 4, 8, 2, 3,
            4, 8, 5, 2, 8, 3, 4, 8, 2, 3,
            4, 2, 8, 3, 4, 2, 8, 3, 4, 5,
            8, 2, 3, 4, 2
    );

    private static final List<String> EXPERIENCE_OPTIONS = List.of(
            "0년", "1년", "2년", "3년", "4년", "5년", "6년", "7년", "8년", "9년", "10년 이상"
    );

    private static final List<String> GENERATED_PROFILE_POSITIONS = List.of(
            "프론트엔드", "백엔드", "풀스택", "디자이너", "IOS", "안드로이드", "데브옵스", "매니저(PM)", "데이터/AI", "QA"
    );

    private static final List<String> PROJECT_PURPOSE_CODES = List.of(
            "PROJECT", "STARTUP", "STUDY", "HACKATHON", "CONTEST", "ENTERPRISE_LINK", "GOVERNMENT_LINK"
    );

    private static final List<String> WORK_METHOD_CODES = List.of("ONLINE", "OFFLINE", "HYBRID");

    private static final List<String> PROJECT_TOPICS = List.of(
            "지역 상권", "여행 기록", "스터디 매칭", "친환경 소비", "의료 예약",
            "음악 큐레이션", "캠퍼스 라이프", "창작 네트워크", "업무 자동화"
    );

    private static final List<String> PROJECT_SURFACES = List.of("플랫폼", "대시보드", "서비스");

    private static final List<String> ORGANIZATION_OPTIONS = List.of(
            "Meetball Studio", "Seoul Product Lab", "Busan Dev Crew", "Incheon Makers",
            "Daegu Growth Team", "Daejeon Builders", "Gwangju Sprint Club", "Jeju Creative Hub"
    );

    private static final List<String> NICKNAME_PREFIXES = List.of(
            "민첩한", "차분한", "집중한", "대담한", "세심한", "유연한", "꾸준한", "반짝이는", "든든한", "기민한"
    );

    private static final List<String> NICKNAME_SUFFIXES = List.of(
            "메이커", "빌더", "코더", "디자이너", "엔지니어", "기획러", "러너", "아키텍트", "리서처", "크리에이터"
    );

    private static final List<String> FAMILY_NAMES = List.of("김", "이", "박", "최", "정", "강", "조", "윤", "장", "임");
    private static final List<String> GIVEN_NAMES = List.of(
            "서준", "서윤", "도윤", "하린", "민재", "예린", "지후", "유진", "현우", "다온"
    );

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final BookmarkedProjectRepository bookmarkedProjectRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final PeerReviewRepository peerReviewRepository;
    private final ProjectReviewRepository projectReviewRepository;
    private final CommentRepository commentRepository;
    private final ProjectResourceRepository projectResourceRepository;
    private final PositionRepository positionRepository;
    private final TechStackRepository techStackRepository;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Override
    @Transactional
    public void run(String... args) {
        assertH2Datasource();
        resetSampleData();

        List<Profile> profiles = seedProfiles();
        List<Project> projects = seedProjects(profiles);

        seedCompatibilityInteractions(profiles, projects);
        assignMissingMemberRoles(profiles, projects);
        populateProjectTeams(profiles, projects);
        seedGeneratedInteractions(profiles, projects);
    }

    private void assertH2Datasource() {
        if (datasourceUrl == null || !datasourceUrl.contains("jdbc:h2:")) {
            throw new IllegalStateException("app.seed.enabled sample reset is only supported on H2.");
        }
    }

    private void resetSampleData() {
        projectReviewRepository.deleteAllInBatch();
        peerReviewRepository.deleteAllInBatch();
        projectResourceRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        viewHistoryRepository.deleteAllInBatch();
        bookmarkedProjectRepository.deleteAllInBatch();
        projectApplicationRepository.deleteAllInBatch();
        projectParticipantRepository.deleteAllInBatch();

        projectRepository.deleteAll();
        projectRepository.flush();

        profileRepository.deleteAll();
        profileRepository.flush();

        accountRepository.deleteAllInBatch();
        accountRepository.flush();
    }

    private List<Profile> seedProfiles() {
        List<Profile> profiles = new ArrayList<>();

        profiles.add(createProfile("leader@meetball.local", "초코푸들", "초코푸들",
                "프론트엔드", "5년 이상", "Meetball Studio", true, "여자",
                LocalDate.of(1996, 3, 14), List.of("React", "TypeScript")));
        profiles.add(createProfile("member@meetball.local", "성실한리트리버", "성실한리트리버",
                "백엔드", "4년", "Busan Dev Crew", true, "남자",
                LocalDate.of(1995, 7, 9), List.of("Java", "Spring")));
        profiles.add(createProfile("guest@meetball.local", "열정고양이", "열정고양이",
                "디자이너", "1년", "Seoul Product Lab", true, "여자",
                LocalDate.of(1998, 7, 15), List.of("Figma", "Zeplin")));
        profiles.add(createProfile("dev@meetball.local", "코딩하는비글", "코딩하는비글",
                "풀스택", "6년", "Daejeon Builders", true, "남자",
                LocalDate.of(1994, 11, 2), List.of("Nextjs", "Nodejs", "React")));

        for (int index = FIXTURE_PROFILE_COUNT + 1; index <= TOTAL_PROFILE_COUNT; index++) {
            profiles.add(createGeneratedProfile(index));
        }

        return profiles;
    }

    private Profile createGeneratedProfile(int index) {
        String positionName = GENERATED_PROFILE_POSITIONS.get((index - 1) % GENERATED_PROFILE_POSITIONS.size());
        String nickname = NICKNAME_PREFIXES.get((index - 1) % NICKNAME_PREFIXES.size())
                + NICKNAME_SUFFIXES.get((index + 2) % NICKNAME_SUFFIXES.size())
                + String.format("%02d", index);
        String name = FAMILY_NAMES.get((index - 1) % FAMILY_NAMES.size())
                + GIVEN_NAMES.get((index * 3) % GIVEN_NAMES.size());
        String email = "sample" + String.format("%02d", index) + "@meetball.local";
        String experienceYears = EXPERIENCE_OPTIONS.get(index % EXPERIENCE_OPTIONS.size());
        String organization = ORGANIZATION_OPTIONS.get(index % ORGANIZATION_OPTIONS.size());
        String gender = index % 2 == 0 ? "남자" : "여자";
        LocalDate birthDate = LocalDate.of(1989 + (index % 10), (index % 12) + 1, (index % 27) + 1);

        return createProfile(
                email,
                name,
                nickname,
                positionName,
                experienceYears,
                organization,
                index % 3 != 0,
                gender,
                birthDate,
                defaultProfileTechStacks(positionName, index)
        );
    }

    private Profile createProfile(String email, String name, String nickname, String positionName,
                                  String experienceYears, String organization, boolean orgVisible,
                                  String gender, LocalDate birthDate, List<String> techStackNames) {
        Profile profile = Profile.builder()
                .email(email)
                .nickname(nickname)
                .isPublic(true)
                .build();
        profile.completeOnboarding(
                name,
                nickname,
                phoneNumberFor(email),
                birthDate,
                gender,
                resolvePosition(positionName),
                experienceYears,
                organization,
                orgVisible,
                resolveTechStacks(techStackNames)
        );
        return profileRepository.save(profile);
    }

    private List<Project> seedProjects(List<Profile> profiles) {
        List<Project> projects = new ArrayList<>();

        projects.add(createProject(
                "AI 기반 헬스케어 모바일 앱 개발",
                "사용자의 건강 데이터를 분석하여 맞춤형 운동 및 식단을 추천하는 AI 헬스케어 앱 개발입니다.",
                "PROJECT",
                "ONLINE",
                "프론트엔드:2, 백엔드:1, 디자이너:1",
                profiles.get(0),
                List.of("ReactNative", "Python"),
                Project.PROGRESS_STATUS_READY,
                1
        ));

        projects.add(createProject(
                "반려견 케어 서비스 [멍멍 비서]",
                "반려견의 일정을 관리하고 산책 메이트를 매칭해주는 서비스입니다.",
                "STARTUP",
                "ONLINE",
                "매니저(PM):1, 프론트엔드:2, 백엔드:2",
                profiles.get(0),
                List.of("Nextjs", "Spring"),
                Project.PROGRESS_STATUS_COMPLETED,
                2
        ));

        projects.add(createProject(
                "여행 일정 공유 플랫폼 [TripMate]",
                "전 세계 여행자들과 일정을 공유하고 동행을 구하는 소셜 플랫폼입니다.",
                "PROJECT",
                "HYBRID",
                "IOS:1, 안드로이드:2, 백엔드:2",
                profiles.get(1),
                List.of("Flutter", "Firebase"),
                Project.PROGRESS_STATUS_READY,
                3
        ));

        for (int index = FIXTURE_PROJECT_COUNT + 1; index <= TOTAL_PROJECT_COUNT; index++) {
            projects.add(createGeneratedProject(index, profiles));
        }

        return projects;
    }

    private Project createGeneratedProject(int index, List<Profile> profiles) {
        int generatedIndex = index - FIXTURE_PROJECT_COUNT;
        Profile owner = profiles.get(generatedIndex + 1);
        String topic = PROJECT_TOPICS.get((generatedIndex - 1) % PROJECT_TOPICS.size());
        String surface = PROJECT_SURFACES.get(((generatedIndex - 1) / PROJECT_TOPICS.size()) % PROJECT_SURFACES.size());
        String title = topic + " " + surface + " " + String.format("%02d", generatedIndex);
        int totalCapacity = plannedProjectCapacity(index);
        String positions = generatedProjectPositions(generatedIndex, owner.getPosition(), totalCapacity);
        String purpose = PROJECT_PURPOSE_CODES.get((generatedIndex - 1) % PROJECT_PURPOSE_CODES.size());
        String workMethod = WORK_METHOD_CODES.get((generatedIndex - 1) % WORK_METHOD_CODES.size());
        String progressStatus = generatedIndex % 5 == 0
                ? Project.PROGRESS_STATUS_COMPLETED
                : (generatedIndex % 4 == 0 ? Project.PROGRESS_STATUS_IN_PROGRESS : Project.PROGRESS_STATUS_READY);
        String description = topic + " 영역의 사용자들이 " + surface.toLowerCase()
                + " 안에서 정보를 공유하고 함께 실행할 수 있도록 설계한 샘플 프로젝트입니다.";

        return createProject(
                title,
                description,
                purpose,
                workMethod,
                positions,
                owner,
                generatedProjectTechStacks(positions, generatedIndex),
                progressStatus,
                index
        );
    }

    private Project createProject(String title, String description, String purpose, String method, String positions,
                                  Profile owner, List<String> techStacks, String progressStatus, int seedIndex) {
        int requiredMember = ProjectSelectionCatalog.totalCapacity(positions);
        LocalDate now = LocalDate.now();
        int spread = Math.max(1, seedIndex % 6);

        LocalDate recruitStartDate;
        LocalDate recruitEndDate;
        LocalDate projectStartDate;
        LocalDate projectEndDate;
        String recruitStatus;

        if (Project.PROGRESS_STATUS_COMPLETED.equals(progressStatus)) {
            recruitStartDate = now.minusDays(30L + spread);
            recruitEndDate = now.minusDays(18L + spread);
            projectStartDate = now.minusDays(15L + spread);
            projectEndDate = now.minusDays(2L + spread);
            recruitStatus = Project.RECRUIT_STATUS_CLOSED;
        } else if (Project.PROGRESS_STATUS_IN_PROGRESS.equals(progressStatus)) {
            recruitStartDate = now.minusDays(18L + spread);
            recruitEndDate = now.minusDays(6L + spread);
            projectStartDate = now.minusDays(3L + spread);
            projectEndDate = now.plusDays(20L - spread);
            recruitStatus = Project.RECRUIT_STATUS_CLOSED;
        } else {
            recruitStartDate = now.minusDays(3L + spread);
            recruitEndDate = now.plusDays(10L + spread);
            projectStartDate = now.plusDays(18L + spread);
            projectEndDate = null;
            recruitStatus = Project.RECRUIT_STATUS_OPEN;
        }

        Project project = new Project(
                title,
                description,
                purpose,
                method,
                requiredMember,
                recruitStartDate,
                recruitEndDate,
                projectStartDate,
                projectEndDate,
                recruitStatus,
                progressStatus,
                LocalDateTime.now().minusDays(12L + spread),
                LocalDateTime.now().minusDays(spread)
        );
        project.assignOwner(owner);
        project.replacePositions(ProjectSelectionCatalog.parsePositionCapacities(positions, requiredMember), this::resolvePosition);
        project.replaceTechStacks(resolveTechStacks(techStacks));
        project.updateThumbnailUrl("");

        Project saved = projectRepository.save(project);
        assignParticipant(saved, owner, "LEADER");
        return saved;
    }

    private void seedCompatibilityInteractions(List<Profile> profiles, List<Project> projects) {
        Profile leader = profiles.get(0);
        Profile member = profiles.get(1);
        Profile guest = profiles.get(2);
        Profile dev = profiles.get(3);

        Project project1 = projects.get(0);
        Project project2 = projects.get(1);
        Project project3 = projects.get(2);

        assignParticipant(project1, member, "MEMBER", findRecruitPosition(project1, "백엔드"));

        projectApplicationRepository.save(ProjectApplication.builder()
                .project(project1)
                .profile(guest)
                .applicantName(guest.getNickname())
                .position("디자이너")
                .recruitPosition(findRecruitPosition(project1, "디자이너"))
                .message("디자인 시스템과 화면 플로우를 함께 만들고 싶습니다.")
                .status(ProjectApplicationStatus.PENDING)
                .build());

        assignParticipant(project2, dev, "MEMBER", findRecruitPosition(project2, "프론트엔드"));

        bookmarkedProjectRepository.save(BookmarkedProject.builder().profile(guest).project(project1).build());
        project1.incrementBookmarkCount();
        bookmarkedProjectRepository.save(BookmarkedProject.builder().profile(guest).project(project3).build());
        project3.incrementBookmarkCount();

        viewHistoryRepository.save(ViewHistory.builder().profile(guest).project(project1).build());
        project1.incrementViewCount();
        viewHistoryRepository.save(ViewHistory.builder().profile(leader).project(project2).build());
        project2.incrementViewCount();

        peerReviewRepository.save(PeerReview.builder()
                .project(project2)
                .reviewer(dev)
                .reviewee(leader)
                .content("리더님의 명확한 디렉션 덕분에 일정 내에 멋진 결과물을 낼 수 있었습니다!")
                .score(5.0)
                .build());
        projectReviewRepository.save(new ProjectReview(project2, leader,
                "완료 후에도 다시 참여하고 싶은 좋은 프로젝트였습니다.", 5.0));
    }

    private void assignMissingMemberRoles(List<Profile> profiles, List<Project> projects) {
        int projectCursor = 0;
        for (Profile profile : profiles) {
            if (hasMemberRole(profile)) {
                continue;
            }

            boolean assigned = assignMemberRole(projects, profile, projectCursor, true);
            if (!assigned) {
                assigned = assignMemberRole(projects, profile, projectCursor, false);
            }

            if (!assigned) {
                throw new IllegalStateException("Unable to assign seed member role for profile: " + profile.getNickname());
            }

            projectCursor = (projectCursor + 1) % projects.size();
        }
    }

    private void populateProjectTeams(List<Profile> profiles, List<Project> projects) {
        int profileCursor = 0;
        for (int index = 0; index < projects.size(); index++) {
            Project project = projects.get(index);
            int targetRecruitment = targetRecruitment(project, index + 1);
            while (project.getCurrentRecruitment() < targetRecruitment) {
                boolean assigned = assignProjectMember(project, profiles, profileCursor, true);
                if (!assigned) {
                    assigned = assignProjectMember(project, profiles, profileCursor, false);
                }
                if (!assigned) {
                    throw new IllegalStateException("Unable to populate seed participants for project: " + project.getTitle());
                }
                profileCursor = (profileCursor + 1) % profiles.size();
            }
        }
    }

    private void seedGeneratedInteractions(List<Profile> profiles, List<Project> projects) {
        List<Profile> candidateProfiles = new ArrayList<>(profiles);

        for (int index = FIXTURE_PROJECT_COUNT; index < projects.size(); index++) {
            Project project = projects.get(index);
            int generatedIndex = index - FIXTURE_PROJECT_COUNT + 1;
            List<Profile> members = projectParticipantRepository.findByProject(project).stream()
                    .filter(participant -> "MEMBER".equals(participant.getRole()))
                    .map(ProjectParticipant::getProfile)
                    .toList();

            if (Project.RECRUIT_STATUS_OPEN.equals(project.getRecruitStatus())) {
                ProjectRecruitPosition recruitPosition = nextOpenRecruitPosition(project);
                if (recruitPosition != null) {
                    Set<Long> excludedIds = participantIds(project);
                    Profile applicant = pickDistinctProfiles(candidateProfiles, excludedIds, 1).get(0);
                    projectApplicationRepository.save(ProjectApplication.builder()
                            .project(project)
                            .profile(applicant)
                            .applicantName(applicant.getNickname())
                            .position(recruitPosition.getPositionName())
                            .recruitPosition(recruitPosition)
                            .message(project.getTitle() + " 프로젝트에 기여하고 싶습니다.")
                            .status(ProjectApplicationStatus.PENDING)
                            .build());
                }
            }

            Profile bookmarker = pickDistinctProfiles(candidateProfiles,
                    Set.of(project.getOwnerProfile().getId()),
                    1).get(0);
            bookmarkedProjectRepository.save(BookmarkedProject.builder()
                    .profile(bookmarker)
                    .project(project)
                    .build());
            project.incrementBookmarkCount();

            List<Profile> viewers = pickDistinctProfiles(candidateProfiles, Set.of(), 2);
            for (Profile viewer : viewers) {
                viewHistoryRepository.save(ViewHistory.builder().profile(viewer).project(project).build());
                project.incrementViewCount();
            }

            if (Project.PROGRESS_STATUS_COMPLETED.equals(project.getProgressStatus()) && !members.isEmpty()) {
                Profile reviewer = members.get(0);
                peerReviewRepository.save(PeerReview.builder()
                        .project(project)
                        .reviewer(reviewer)
                        .reviewee(project.getOwnerProfile())
                        .content("진행 구조가 탄탄해서 끝까지 안정적으로 협업할 수 있었습니다.")
                        .score(4.0 + (generatedIndex % 2))
                        .build());
                projectReviewRepository.save(new ProjectReview(project, reviewer,
                        "샘플 완료 프로젝트 검증용 리뷰입니다.", 4.0 + (generatedIndex % 2)));
            }
        }
    }

    private String generatedProjectPositions(int generatedIndex, String ownerPosition, int totalCapacity) {
        List<String> positionPool = List.of(
                "프론트엔드", "백엔드", "풀스택", "디자이너", "IOS", "안드로이드", "데브옵스", "매니저(PM)", "데이터/AI", "QA"
        );
        List<Integer> capacities = switch (totalCapacity) {
            case 2 -> List.of(1, 1);
            case 3 -> List.of(1, 1, 1);
            case 4 -> List.of(2, 1, 1);
            case 5 -> List.of(2, 2, 1);
            case 8 -> List.of(2, 2, 2, 2);
            default -> throw new IllegalStateException("Unsupported seed project capacity: " + totalCapacity);
        };
        List<String> selected = new ArrayList<>();
        if (ownerPosition != null && !ownerPosition.isBlank()) {
            selected.add(ownerPosition);
        }
        int cursor = generatedIndex % positionPool.size();
        while (selected.size() < capacities.size()) {
            String candidate = positionPool.get(cursor % positionPool.size());
            if (!selected.contains(candidate)) {
                selected.add(candidate);
            }
            cursor += 3;
        }

        List<String> positionSpecs = new ArrayList<>();
        for (int index = 0; index < capacities.size(); index++) {
            positionSpecs.add(selected.get(index) + ":" + capacities.get(index));
        }
        return String.join(", ", positionSpecs);
    }

    private List<String> generatedProjectTechStacks(String positions, int generatedIndex) {
        Set<String> selected = new LinkedHashSet<>();
        for (ProjectSelectionCatalog.PositionCapacity capacity : ProjectSelectionCatalog.parsePositionCapacities(positions, null)) {
            selected.addAll(defaultProjectTechStacks(capacity.name(), generatedIndex));
            if (selected.size() >= 3) {
                break;
            }
        }
        if (selected.isEmpty()) {
            selected.add("Git");
            selected.add("Docker");
        }
        return new ArrayList<>(selected).subList(0, Math.min(3, selected.size()));
    }

    private List<String> defaultProfileTechStacks(String positionName, int index) {
        return defaultProjectTechStacks(positionName, index);
    }

    private List<String> defaultProjectTechStacks(String positionName, int index) {
        return switch (positionName) {
            case "프론트엔드" -> List.of("React", "TypeScript", index % 2 == 0 ? "Nextjs" : "Vue");
            case "백엔드" -> List.of("Java", "Spring", index % 2 == 0 ? "MySQL" : "Nodejs");
            case "풀스택" -> List.of("Nextjs", "Nodejs", "React");
            case "디자이너" -> List.of("Figma", "Zeplin");
            case "IOS" -> List.of("Swift", "Firebase");
            case "안드로이드" -> List.of("Kotlin", "Firebase");
            case "데브옵스" -> List.of("AWS", "Docker", "Kubernetes");
            case "매니저(PM)" -> List.of("Figma", "Zeplin", "Git");
            case "데이터/AI" -> List.of("Python", "Django", "MongoDB");
            case "QA" -> List.of("Jest", "Git", "Docker");
            default -> List.of("Git", "Docker");
        };
    }

    private String phoneNumberFor(String email) {
        int numeric = Math.abs(email.hashCode());
        return String.format("010-%04d-%04d", numeric % 10000, (numeric / 10000) % 10000);
    }

    private boolean hasMemberRole(Profile profile) {
        return projectParticipantRepository.findByProfile(profile).stream()
                .anyMatch(participant -> "MEMBER".equals(participant.getRole()));
    }

    private boolean assignParticipant(Project project, Profile profile, String role) {
        return assignParticipant(project, profile, role, null);
    }

    private boolean assignParticipant(Project project, Profile profile, String role, ProjectRecruitPosition preferredPosition) {
        if (projectParticipantRepository.existsByProjectAndProfile(project, profile)
                || projectApplicationRepository.existsByProjectAndProfile(project, profile)) {
            return false;
        }

        ProjectRecruitPosition assignedPosition = preferredPosition != null
                ? preferredPosition
                : selectAssignablePosition(project, profile);
        if (assignedPosition == null || assignedPosition.getApprovedUser() >= assignedPosition.getCapacity()) {
            return false;
        }

        assignedPosition.incrementApprovedUser();
        projectParticipantRepository.save(ProjectParticipant.builder()
                .project(project)
                .profile(profile)
                .recruitPosition(assignedPosition)
                .role(role)
                .build());
        return true;
    }

    private boolean assignMemberRole(List<Project> projects, Profile profile, int projectCursor, boolean requireMatchingPosition) {
        for (int offset = 0; offset < projects.size(); offset++) {
            Project candidate = projects.get((projectCursor + offset) % projects.size());
            if (requireMatchingPosition && !hasOpenPosition(candidate, profile.getPosition())) {
                continue;
            }
            if (assignParticipant(candidate, profile, "MEMBER")) {
                return true;
            }
        }
        return false;
    }

    private boolean assignProjectMember(Project project, List<Profile> profiles, int profileCursor, boolean requireMatchingPosition) {
        for (int offset = 0; offset < profiles.size(); offset++) {
            Profile candidate = profiles.get((profileCursor + offset) % profiles.size());
            if (projectParticipantRepository.existsByProjectAndProfile(project, candidate)) {
                continue;
            }
            if (requireMatchingPosition && !hasOpenPosition(project, candidate.getPosition())) {
                continue;
            }
            if (assignParticipant(project, candidate, "MEMBER")) {
                return true;
            }
        }
        return false;
    }

    private int targetRecruitment(Project project, int seedIndex) {
        int totalRecruitment = project.getTotalRecruitment() != null ? project.getTotalRecruitment() : 0;
        if (Project.PROGRESS_STATUS_COMPLETED.equals(project.getProgressStatus())
                || Project.PROGRESS_STATUS_IN_PROGRESS.equals(project.getProgressStatus())) {
            return totalRecruitment;
        }
        if (totalRecruitment <= 2) {
            return totalRecruitment;
        }
        if (totalRecruitment <= 4) {
            return totalRecruitment - 1;
        }
        if (totalRecruitment == 5) {
            return 4;
        }
        return 5 + (seedIndex % 2);
    }

    private int plannedProjectCapacity(int seedIndex) {
        if (seedIndex < 1 || seedIndex > PROJECT_CAPACITY_PLAN.size()) {
            throw new IllegalStateException("Unsupported seed project index: " + seedIndex);
        }
        return PROJECT_CAPACITY_PLAN.get(seedIndex - 1);
    }

    private ProjectRecruitPosition selectAssignablePosition(Project project, Profile profile) {
        List<ProjectRecruitPosition> availablePositions = project.getPositionSelections().stream()
                .filter(position -> position.getApprovedUser() < position.getCapacity())
                .toList();
        if (availablePositions.isEmpty()) {
            return null;
        }

        String preferredPosition = profile != null ? profile.getPosition() : null;
        return availablePositions.stream()
                .filter(position -> position.getPositionName().equals(preferredPosition))
                .findFirst()
                .orElse(availablePositions.get(0));
    }

    private ProjectRecruitPosition nextOpenRecruitPosition(Project project) {
        return project.getPositionSelections().stream()
                .filter(position -> position.getApprovedUser() < position.getCapacity())
                .findFirst()
                .orElse(null);
    }

    private boolean hasOpenPosition(Project project, String positionName) {
        if (positionName == null || positionName.isBlank()) {
            return false;
        }
        return project.getPositionSelections().stream()
                .anyMatch(position -> positionName.equals(position.getPositionName())
                        && position.getApprovedUser() < position.getCapacity());
    }

    private Set<Long> participantIds(Project project) {
        Set<Long> excludedIds = new LinkedHashSet<>();
        excludedIds.add(project.getOwnerProfile().getId());
        projectParticipantRepository.findByProject(project).stream()
                .map(ProjectParticipant::getProfile)
                .filter(profile -> profile != null && profile.getId() != null)
                .map(Profile::getId)
                .forEach(excludedIds::add);
        return excludedIds;
    }

    private List<Profile> pickDistinctProfiles(List<Profile> candidates, Set<Long> excludedIds, int count) {
        List<Profile> selected = new ArrayList<>();
        for (Profile candidate : candidates) {
            if (excludedIds.contains(candidate.getId())) {
                continue;
            }
            selected.add(candidate);
            if (selected.size() == count) {
                break;
            }
        }
        if (selected.size() < count) {
            throw new IllegalStateException("Not enough seed profiles to satisfy sample data generation.");
        }
        return selected;
    }

    private Position resolvePosition(String positionName) {
        return positionRepository.findByName(positionName)
                .orElseThrow(() -> new IllegalStateException("Seed position not found: " + positionName));
    }

    private List<TechStack> resolveTechStacks(List<String> techStackNames) {
        return techStackNames.stream()
                .map(techStackName -> techStackRepository.findByName(techStackName)
                        .orElseThrow(() -> new IllegalStateException("Seed tech stack not found: " + techStackName)))
                .toList();
    }

    private ProjectRecruitPosition findRecruitPosition(Project project, String positionName) {
        return project.getPositionSelections().stream()
                .filter(position -> positionName.equals(position.getPositionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed project position not found: " + positionName));
    }
}
