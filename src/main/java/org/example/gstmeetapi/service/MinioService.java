package org.example.gstmeetapi.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class MinioService {
    private final MinioClient minioClient;
    @Value("${minio.bucketName}")
    private String bucketName;

    public MinioService(@Value("${minio.url}") String url,
                        @Value("${minio.accessKey}") String accessKey,
                        @Value("${minio.secretKey}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void uploadFile(String objectName, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fis, file.length(), -1)
                            .contentType(Files.probeContentType(file.toPath()))
                            .build()
            );
            System.out.println("Uploaded: " + objectName);
        } catch (Exception e) {
            System.err.println("Error uploading to MinIO: " + e.getMessage());
        }
    }

    public void uploadDirectory(String roomId, String nick, String folderPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Folder does not exist: " + folderPath);
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files to upload in: " + folderPath);
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                String minioPath = roomId + "/" + nick + "/" + file.getName();
                uploadFile(minioPath, file);
            }
        }
    }


}
