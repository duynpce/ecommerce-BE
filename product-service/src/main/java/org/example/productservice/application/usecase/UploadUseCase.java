package org.example.productservice.application.usecase;

import org.springframework.web.multipart.MultipartFile;

public interface UploadUseCase {
    String uploadImg(MultipartFile img);
}