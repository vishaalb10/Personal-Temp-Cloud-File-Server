package com.codewithvishaal.personalcloudfileserver;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    // Uploads a file and tags it with the given passkey
    public Map upload(MultipartFile file, String passkey) throws IOException {

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }

        String extension = "";

        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(
                    originalFilename.lastIndexOf(".") + 1
            ).toLowerCase();
        }

        String baseName = originalFilename;

        if (originalFilename.contains(".")) {
            baseName = originalFilename.substring(
                    0,
                    originalFilename.lastIndexOf(".")
            );
        }

        // Make the filename safe for Cloudinary
        String safeName = baseName.replaceAll(
                "[^a-zA-Z0-9-_]",
                "_"
        );

        String ext = extension.isEmpty()
                ? ""
                : "." + extension;

        List<String> imageLikeExtensions = List.of(
                "jpg", "jpeg", "png", "gif",
                "webp", "bmp", "svg", "pdf",
                "tiff", "ico"
        );

        Map<String, Object> options = new HashMap<>();

        options.put("tags", passkey);

        // Start with the original filename
        String finalName = safeName;

        // If the name already exists, add _1, _2, etc.
        int number = 1;

        while (fileNameExists(
                "personal-cloud-uploads/" + finalName + ext
        )) {

            finalName = safeName + "_" + number;
            number++;
        }

        if (imageLikeExtensions.contains(extension)) {

            options.put("folder", "personal-cloud-uploads");
            options.put("resource_type", "auto");
            options.put("public_id", finalName);

        } else {

            options.put("resource_type", "raw");
            options.put(
                    "public_id",
                    "personal-cloud-uploads/" + finalName + ext
            );
        }

        return cloudinary.uploader().upload(
                file.getBytes(),
                options
        );
    }

    // Checks whether a filename already exists
    private boolean fileNameExists(String publicId) {

        for (String type : new String[]{"image", "raw", "video"}) {

            try {

                Map result = cloudinary.api().resource(
                        publicId,
                        ObjectUtils.asMap(
                                "resource_type", type
                        )
                );

                if (result != null) {
                    return true;
                }

            } catch (Exception ignored) {
                // File doesn't exist with this resource type
            }
        }

        return false;
    }

    // Fetches all files tagged with a given passkey
    public List<Map> getFilesByPasskey(String passkey) throws Exception {

        List<Map> allFiles = new java.util.ArrayList<>();

        String[] resourceTypes = {
                "image",
                "raw",
                "video"
        };

        for (String type : resourceTypes) {

            try {

                Map result = cloudinary.api().resourcesByTag(
                        passkey,
                        ObjectUtils.asMap(
                                "resource_type",
                                type
                        )
                );

                List<Map> resources =
                        (List<Map>) result.get("resources");

                if (resources != null) {
                    allFiles.addAll(resources);
                }

            } catch (Exception ignored) {
                // No files of this type — that's okay
            }
        }

        return allFiles;
    }

    // Deletes a file only if it belongs to the passkey
    public void deleteFile(
            String publicId,
            String passkey
    ) throws Exception {

        List<Map> files =
                getFilesByPasskey(passkey);

        Map target = files.stream()
                .filter(f ->
                        f.get("public_id")
                                .equals(publicId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new SecurityException(
                                "Passkey does not own this file"
                        )
                );

        String resourceType =
                (String) target.get("resource_type");

        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap(
                        "resource_type",
                        resourceType
                )
        );
    }

    // Generates a random 4-digit passkey
    public String generatePasskey() throws Exception {

        String candidate;
        boolean exists;

        do {

            SecureRandom random =
                    new SecureRandom();

            int number =
                    random.nextInt(100000);

            candidate =
                    String.format("%05d", number);

            exists =
                    isPasskeyInUse(candidate);

        } while (exists);

        return candidate;
    }

    private boolean isPasskeyInUse(
            String passkey
    ) throws Exception {

        for (String type :
                new String[]{"image", "raw", "video"}) {

            try {

                Map result =
                        cloudinary.api().resourcesByTag(
                                passkey,
                                ObjectUtils.asMap(
                                        "resource_type",
                                        type,
                                        "max_results",
                                        1
                                )
                        );

                List<Map> resources =
                        (List<Map>) result.get(
                                "resources"
                        );

                if (resources != null &&
                        !resources.isEmpty()) {

                    return true;
                }

            } catch (Exception ignored) {
            }
        }

        return false;
    }
}