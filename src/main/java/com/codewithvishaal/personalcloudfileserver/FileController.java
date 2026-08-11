package com.codewithvishaal.personalcloudfileserver;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class FileController {

    private final CloudinaryService cloudinaryService;

    public FileController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "passkey", required = false) String passkey) throws Exception {

        String finalPasskey = (passkey == null || passkey.isBlank())
                ? cloudinaryService.generatePasskey()
                : passkey;

        Map result = cloudinaryService.upload(file, finalPasskey);

        return ResponseEntity.ok(Map.of(
                "passkey", finalPasskey,
                "url", result.get("secure_url"),
                "publicId", result.get("public_id")
        ));
    }

    @GetMapping("/files/{passkey}")
    public ResponseEntity<?> getFiles(@PathVariable String passkey) throws Exception {
        return ResponseEntity.ok(cloudinaryService.getFilesByPasskey(passkey));
    }

    @DeleteMapping("/files/{passkey}/{publicId}")
    public ResponseEntity<?> delete(
            @PathVariable String passkey,
            @PathVariable String publicId) throws Exception {

        cloudinaryService.deleteFile(publicId.replace("--", "/"), passkey);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}