package com.trithuc.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.trithuc.service.FileStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileStoreServiceImpl implements FileStoreService {

    private final Path rootLocation = Paths.get("uploads");

    @Value("dhitvvwzj")
    private String cloudName;
    @Value("352255363859438")
    private String apiKey;
    @Value("Dg-ybZD8C13nY-EfQk1S3SL4LYA")
    private String apiSecret;
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initial storage");
        }
    }

    @Override
    public void saveImage(MultipartFile file, Long identifier, String type) {
        try {
            Path destinationDirectory = this.rootLocation.resolve(type);
            if (!Files.exists(destinationDirectory)) {
                Files.createDirectories(destinationDirectory);
            }
            Path destinationFile = destinationDirectory.resolve(identifier + "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store File", e);
        }
    }

    @Override
    public List<String> getAllImageNames(String type) {
        try {
            Path destinationDirectory = this.rootLocation.resolve(type);
            if (Files.exists(destinationDirectory)) {
                return Files.walk(destinationDirectory, 1)
                        .filter(path -> !path.equals(destinationDirectory))
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
        return List.of(); // Trả về danh sách rỗng nếu không tìm thấy
    }

    @Override
    public Resource loadImage(String filename, String type) {
        try {
            Path file = rootLocation.resolve(type).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            System.out.println(filename);
            System.out.println(resource);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error " + e.getMessage());
        }
    }
    @Override
    public void deleteOldImage(String type,String oldImageName) {
        try {
            // Tạo đường dẫn tới thư mục chứa ảnh
            Path destinationDirectory = this.rootLocation.resolve(type);
            if (Files.exists(destinationDirectory)) {


                Path oldImagePath = destinationDirectory.resolve(oldImageName);
                if (Files.exists(oldImagePath)) {
                    // Nếu ảnh cũ tồn tại, xóa nó
                    Files.delete(oldImagePath);
                    System.out.println("Successfully deleted old image: " + oldImageName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete old image", e);
        }
    }
    @Override
    public String saveImageCloudinary(MultipartFile file) throws IOException{
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name",cloudName,
                "api_key",apiKey,
                "api_secret",apiSecret
        ));
        Map<?,?> saveImage = cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap("resource_type","image"));
        return  (String) saveImage.get("url");
    }

    @Override
    public List<String> saveListImagesCloudinary(List<MultipartFile> files) throws IOException {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "cloudName",
                "api_key", "apiKey",
                "api_secret", "apiSecret"
        ));

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            Map<?, ?> saveImage = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            imageUrls.add((String) saveImage.get("url"));
        }
        return imageUrls;
    }


}
