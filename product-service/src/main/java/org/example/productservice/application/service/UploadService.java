package org.example.productservice.application.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.example.productservice.application.usecase.UploadUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class UploadService implements UploadUseCase {

    private static final String GCS_BASE_URL = "https://storage.googleapis.com";

    private final Storage storage;
    private final String bucketName;

    public UploadService(
            @Value("${gcp.bucket-name}") String bucketName,
            @Value("${gcp.config-path}") String configPath
    ) throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(configPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        this.storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        this.bucketName = bucketName;
    }

    @Override
    public String uploadImg(MultipartFile img) {
        validateImg(img);

        try {
            String fileName = UUID.randomUUID() + "_" + img.getOriginalFilename();

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(img.getContentType())
                    .build();

            storage.create(blobInfo, img.getBytes());

            return String.format("%s/%s/%s", GCS_BASE_URL, bucketName, fileName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Google Cloud Storage", e);
        }
    }

    private void validateImg(MultipartFile img) {
        if (img == null || img.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }

        String contentType = img.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Invalid file type: only image files are accepted (e.g. image/jpeg, image/png, image/webp)"
            );
        }
    }
}