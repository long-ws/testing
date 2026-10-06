package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class ImageUtils {

    private static volatile String cachedImageDir = null;
    private static volatile ServletContext cachedServletContext = null;

    public static void init(ServletContext servletContext) {
        if (cachedServletContext == servletContext && cachedImageDir != null) {
            return;
        }
        synchronized (ImageUtils.class) {
            if (cachedServletContext != servletContext || cachedImageDir == null) {
                cachedServletContext = servletContext;
                cachedImageDir = resolveImageDir(servletContext);
            }
        }
    }

    private static String resolveImageDir(ServletContext servletContext) {
        String realPath = servletContext.getRealPath("/images");
        if (realPath != null) {
            Path dir = Paths.get(realPath);
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return realPath;
        }
        String configured = ConstantUtils.getImageStoragePath();
        Path dir = Paths.get(configured);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return configured;
    }

    public static Path getImageDir() {
        if (cachedImageDir == null) {
            throw new IllegalStateException("ImageUtils.init(ServletContext) must be called before using ImageUtils");
        }
        return Paths.get(cachedImageDir);
    }

    public static String uploadSingle(HttpServletRequest request) {
        return uploadSingle(request, "image");
    }

    public static String uploadSingle(HttpServletRequest request, String formFieldName) {
        if (cachedImageDir == null) {
            init(request.getServletContext());
        }
        try {
            Part filePart = request.getPart(formFieldName);
            if (filePart == null || filePart.getSize() == 0) {
                return null;
            }

            String contentType = filePart.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return null;
            }

            String originalName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String savedName = generateFileName(originalName);
            Path targetPath = Paths.get(cachedImageDir).resolve(savedName);

            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return savedName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> uploadMultiple(HttpServletRequest request, String paramName) {
        if (cachedImageDir == null) {
            init(request.getServletContext());
        }
        List<String> uploadedFiles = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                String partName = part.getName();
                if (!paramName.equals(partName)) continue;
                if (part.getSize() == 0) continue;
                String contentType = part.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) continue;
                String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                if (originalName == null || originalName.trim().isEmpty()) continue;
                String savedName = generateFileName(originalName);
                Path targetPath = Paths.get(cachedImageDir).resolve(savedName);
                try (InputStream input = part.getInputStream()) {
                    Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                uploadedFiles.add(savedName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadedFiles;
    }

    public static void delete(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) return;
        if (cachedImageDir == null) return;
        Path imagePath = Paths.get(cachedImageDir).resolve(imageName);
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateFileName(String originalName) {
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + ext;
    }
}
