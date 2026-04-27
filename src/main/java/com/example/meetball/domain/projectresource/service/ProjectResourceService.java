package com.example.meetball.domain.projectresource.service;

import com.example.meetball.domain.projectresource.dto.ProjectResourceResponseDto;
import com.example.meetball.domain.projectresource.entity.ProjectResource;
import com.example.meetball.domain.projectresource.repository.ProjectResourceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectResourceService {

    private final ProjectResourceRepository projectResourceRepository;

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    @Transactional
    public ProjectResourceResponseDto uploadFile(Long projectId, MultipartFile file, String tabType) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ProjectResource file is required.");
            }
            String originalFilename = file.getOriginalFilename();
            String safeOriginalFilename = StringUtils.cleanPath(originalFilename == null ? "projectResource" : originalFilename);
            if (safeOriginalFilename.contains("..")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
            }
            String storedFileName = UUID.randomUUID() + "_" + safeOriginalFilename;
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path.");
            }
            
            // 물리적 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 메타데이터 DB 저장
            ProjectResource projectResource = ProjectResource.builder()
                    .projectId(projectId)
                    .originalFileName(safeOriginalFilename)
                    .storedFilePath(storedFileName) 
                    .type("FILE")
                    .fileSize(file.getSize())
                    .tabType(tabType)
                    .build();

            ProjectResource savedProjectResource = projectResourceRepository.save(projectResource);
            return new ProjectResourceResponseDto(savedProjectResource);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectResourceResponseDto> getProjectResources(Long projectId) {
        return projectResourceRepository.findByProjectId(projectId).stream()
                .map(ProjectResourceResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long projectId, Long resourceId) {
        try {
            ProjectResource projectResource = findProjectResourceInProject(projectId, resourceId);

            Path filePath = resolveStoredFilePath(projectResource.getStoredFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + projectResource.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found", ex);
        }
    }

    @Transactional(readOnly = true)
    public String getOriginalFileName(Long projectId, Long resourceId) {
        return findProjectResourceInProject(projectId, resourceId).getOriginalFileName();
    }

    @Transactional
    public ProjectResourceResponseDto uploadLink(Long projectId, String title, String url, String tabType) {
        String safeTitle = StringUtils.hasText(title) ? title.trim() : "관련 링크";
        String safeUrl = validateHttpUrl(url);
        ProjectResource projectResource = ProjectResource.builder()
                .projectId(projectId)
                .originalFileName(safeTitle)
                .linkUrl(safeUrl)
                .type("LINK")
                .tabType(tabType)
                .build();
        
        ProjectResource saved = projectResourceRepository.save(projectResource);
        return new ProjectResourceResponseDto(saved);
    }

    @Transactional
    public void deleteProjectResources(Long projectId) {
        List<ProjectResource> resources = projectResourceRepository.findByProjectId(projectId);
        for (ProjectResource projectResource : resources) {
            deletePhysicalFile(projectResource);
        }
        projectResourceRepository.deleteAll(resources);
    }

    @Transactional
    public void deleteResource(Long projectId, Long resourceId) {
        ProjectResource projectResource = findProjectResourceInProject(projectId, resourceId);
        deletePhysicalFile(projectResource);
        projectResourceRepository.delete(projectResource);
    }

    private void deletePhysicalFile(ProjectResource projectResource) {
        if ("FILE".equals(projectResource.getType()) && StringUtils.hasText(projectResource.getStoredFilePath())) {
            try {
                Files.deleteIfExists(resolveStoredFilePath(projectResource.getStoredFilePath()));
            } catch (IOException e) {
                throw new RuntimeException("Could not delete projectResource file: " + projectResource.getOriginalFileName(), e);
            }
        }
    }

    @Transactional
    public ProjectResourceResponseDto updateResource(Long projectId, Long resourceId, String title, String url) {
        ProjectResource projectResource = findProjectResourceInProject(projectId, resourceId);
        
        if (StringUtils.hasText(title)) {
            String newTitle = title.trim();
            if ("FILE".equals(projectResource.getType())) {
                // 파일의 경우 기존 확장자 보존 처리 (옵션)
                String oldName = projectResource.getOriginalFileName();
                int dotIndex = oldName.lastIndexOf(".");
                if (dotIndex > 0 && !newTitle.contains(".")) {
                    newTitle += oldName.substring(dotIndex);
                }
            }
            projectResource.updateOriginalFileName(newTitle);
        }

        if ("LINK".equals(projectResource.getType()) && StringUtils.hasText(url)) {
            projectResource.updateLinkUrl(validateHttpUrl(url));
        }

        return new ProjectResourceResponseDto(projectResourceRepository.save(projectResource));
    }

    private String validateHttpUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link URL is required.");
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only HTTP/HTTPS links are allowed.");
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid link URL.", e);
        }
    }

    private ProjectResource findProjectResourceInProject(Long projectId, Long resourceId) {
        ProjectResource projectResource = projectResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id: " + resourceId));
        if (!projectResource.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project.");
        }
        return projectResource;
    }

    private Path resolveStoredFilePath(String storedFilePath) {
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadRoot.resolve(storedFilePath).normalize();
        if (!filePath.startsWith(uploadRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path.");
        }
        return filePath;
    }
}
