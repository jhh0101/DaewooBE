package com.example.daewoo.accommodation.image.service;

import com.example.daewoo.accommodation.image.dto.ComImageDetailDto;
import com.example.daewoo.accommodation.image.dto.ComImageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComImageService {
    @Autowired
    private ComImageRepository comImageRepository;

    @Value("${file.upload.hotel.path}")
    private String hotelImagePath;

//    public Resource loadImage(String filename) {
//        try {
//            // Load image from classpath
//            String classpath = "classpath:static/hotelimage/" + filename;
//            Resource resource = new ClassPathResource("static/hotelimage/" + filename);
//
//            if (resource.exists() && resource.isReadable()) {
//                return resource;
//            } else {
//                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + filename);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("이미지 로드 중 오류가 발생했습니다: " + filename, e);
//        }
//    }
//
//    public String getMimeType(Resource resource) throws IOException {
//        String contentType = Files.probeContentType(resource.getFile().toPath());
//        return contentType != null ? contentType : "application/octet-stream";
//    }

    public Resource loadImage(String filename) {
        try {
            Path filePath = Paths.get(hotelImagePath).resolve(filename).normalize();
            log.info("filePath : {}", filePath.toString());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 로드 중 오류가 발생했습니다: " + filename, e);
        }
    }

    public String getMimeType(Resource resource) throws IOException {
        String contentType = Files.probeContentType(resource.getFile().toPath());
        return contentType != null ? contentType : "application/octet-stream";
    }

    public ComImageDetailDto findComImage(Long comId){
        List<ComImageEntity> entities = this.comImageRepository.findByAccommodation_ComId(comId);

        String mainImage = entities.stream()
                .filter(ComImageEntity::getIsMain)
                .map(ComImageEntity::getImageUrl)
                .findFirst()
                .orElse(null);

        List<String> subImages = entities.stream()
                .filter(image -> !image.getIsMain())
                .map(ComImageEntity::getImageUrl)
                .toList();

        return new ComImageDetailDto(comId, mainImage, subImages);
    }

}
