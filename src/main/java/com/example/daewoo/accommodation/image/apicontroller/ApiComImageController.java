package com.example.daewoo.accommodation.image.apicontroller;

import com.example.daewoo.accommodation.image.dto.ComImageDetailDto;
import com.example.daewoo.accommodation.image.service.ComImageService;
import com.example.daewoo.common.CommonRestController;
import com.example.daewoo.common.ResponseCode;
import com.example.daewoo.common.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("api/accommodation/images")
public class ApiComImageController extends CommonRestController {

    @Autowired
    private ComImageService comImageService;

// For backward compatibility with existing image URLs
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> loadImageOld(@PathVariable String filename) {
        return loadImage(filename);
    }
    
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> loadImage(@PathVariable String filename) {
        try {
            // Load image from service
            Resource resource = comImageService.loadImage(filename);
            
            // Get MIME type
            String contentType = comImageService.getMimeType(resource);
            
            // Return image with proper headers
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Image load error for filename {}: {}", filename, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/com/{comId}")
    public ResponseEntity<ResponseDto> findComImage(@PathVariable Long comId){
        try{
            ComImageDetailDto dto = this.comImageService.findComImage(comId);
            return getResponseEntity(ResponseCode.SUCCESS, "Image Select Ok", dto, null);
        }catch (Throwable e){
            log.error(e.toString());
            return getResponseEntity(ResponseCode.SELECT_FAIL, "Image Select Error", null, e);
        }
    }


}
