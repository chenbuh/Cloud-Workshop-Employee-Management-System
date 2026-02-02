package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("File is empty");
        }

        // Create directory if not exists
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Save file
        File dest = new File(uploadPath + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return Result.error("Failed to save file: " + e.getMessage());
        }

        // Return file info
        Map<String, Object> map = new HashMap<>();
        map.put("fileName", fileName);
        map.put("originalName", originalFilename);
        // The URL will be /api/v1/files/{fileName} as per WebConfig mapping
        map.put("url", "/api/v1/files/" + fileName);

        return Result.success(map);
    }
}
