package com.example.batch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private static final String OUTPUT_DIR = "app/output";

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job exportCustomerJob;


    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerBatchJob() {
        Map<String, Object> response = new HashMap<>();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();


            new Thread(() -> {
                try {
                    jobLauncher.run(exportCustomerJob, jobParameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            response.put("status", "success");
            response.put("message", "Batch job triggered successfully!");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> listFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists() || !outputDir.isDirectory()) {
                response.put("status", "error");
                response.put("message", "Output directory not found");
                response.put("files", Collections.emptyList());
                return ResponseEntity.ok(response);
            }

            File[] files = outputDir.listFiles((dir, name) -> name.endsWith(".xlsx"));
            if (files == null || files.length == 0) {
                response.put("status", "success");
                response.put("message", "No Excel files found");
                response.put("files", Collections.emptyList());
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> fileList = Arrays.stream(files)
                    .map(file -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getName());
                        fileInfo.put("size", file.length());
                        fileInfo.put("lastModified", file.lastModified());
                        fileInfo.put("downloadUrl", "/api/batch/download/" + file.getName());
                        return fileInfo;
                    })
                    .sorted(Comparator.comparingLong(f -> -((Long) f.get("lastModified"))))
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("message", "Files retrieved successfully");
            response.put("count", fileList.size());
            response.put("files", fileList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error listing files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            if (!filename.endsWith(".xlsx")) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = Paths.get(OUTPUT_DIR, filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/download/today")
    public ResponseEntity<Resource> downloadTodayFile() {
        String todayFileName = String.format("customers_%s.xlsx",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        return downloadFile(todayFileName);
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Batch Customer Export");
        response.put("timestamp", LocalDate.now().toString());
        return ResponseEntity.ok(response);
    }
}
