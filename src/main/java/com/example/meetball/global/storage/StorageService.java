package com.example.meetball.global.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    /**
     * 파일을 저장하고 저장된 파일의 경로(또는 식별자)를 반환합니다.
     */
    String store(MultipartFile file) throws IOException;

    /**
     * 저장된 파일을 리소스로 불러옵니다.
     */
    Resource load(String storedFileName);

    /**
     * 저장된 파일을 삭제합니다.
     */
    void delete(String storedFileName) throws IOException;
}
