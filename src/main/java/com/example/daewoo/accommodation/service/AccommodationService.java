package com.example.daewoo.accommodation.service;

import com.example.daewoo.accommodation.dto.AccommodationAllDto;
import com.example.daewoo.accommodation.dto.AccommodationDiscountDto;
import com.example.daewoo.accommodation.dto.AccommodationEntity;

import com.example.daewoo.accommodation.dto.AccommodationOneDto;
import com.example.daewoo.accommodation.image.dto.ComImageEntity; // [✅ Import 추가]
import com.example.daewoo.accommodation.image.service.ComImageRepository;
import com.example.daewoo.parlor.roomtype.AccRoomTypeDto;
import com.example.daewoo.parlor.roomtype.AccRoomTypeEntity;
import com.example.daewoo.parlor.service.AccRoomTypeRepository;
import com.example.daewoo.wish.service.WishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccommodationService {
    @Autowired
    private ComImageRepository comImageRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private AccRoomTypeRepository accRoomTypeRepository;

    @Value("${file.upload.hotel.path}")
    private String uploadDir;

    public Long totalCount(){
        return accommodationRepository.hotelCount();
    }

    public Slice<AccommodationAllDto> findAll(Integer minPrice, Integer maxPrice, List<String> amCategory, String comTitle, Integer star, String comCategory, Pageable pageable, Long userId) {
        Specification<AccommodationEntity> spec = AccommodationSpecification.hasPriceInRange(minPrice, maxPrice);
        spec = spec.and(AccommodationSpecification.hasAmenities(amCategory));
        spec = spec.and(AccommodationSpecification.hasName(comTitle));
        spec = spec.and(AccommodationSpecification.hasStar(star));
        spec = spec.and(AccommodationSpecification.hasComCategory(comCategory));

        Slice<AccommodationEntity> entities = accommodationRepository.findAll(spec, pageable);
        Slice<AccommodationAllDto> dto = entities.map(AccommodationAllDto::fromEntity);
        List<AccommodationAllDto> list = dto.getContent();
        if (list.isEmpty()) {
            return dto;
        }

        // 2. 찜 여부 확인을 위한 로직 추가
        Set<Long> favoritedComIds = new HashSet<>();

        // 로그인한 사용자(userId != null)인 경우에만 찜 목록을 조회
        if (userId != null) {
            // 현재 페이지에 있는 숙소들의 ID 목록을 추출
            List<Long> comIdsOnPage = list.stream()
                    .map(AccommodationAllDto::getComId)
                    .collect(Collectors.toList());

            // 추출된 ID 목록으로 찜 테이블을 **단 한 번만** 조회
            favoritedComIds = wishRepository.findFavoriteComIdsByUserId(userId, comIdsOnPage);
        }

        // 3. 각 DTO에 추가 정보(이미지, 가격, 찜 여부) 설정
        // final 키워드를 사용하기 위해 favoritedComIds를 final로 다시 할당 (람다식 내에서 사용)
        final Set<Long> finalFavoritedComIds = favoritedComIds;
        list.forEach(item -> {
            Long comId = item.getComId();
            Integer price = accommodationRepository.findLowestPriceByHotelId(comId);
            String image = accommodationRepository.findMainComImage(comId);
            Integer amenitiesCount = accommodationRepository.countAmenitiesByAccommodationId(comId);

            item.setAmenitiesCount(amenitiesCount);
            item.setImage(image);
            item.setPrice(price);

            // 찜 목록 Set에 현재 숙소 ID가 포함되어 있는지 확인하여 true/false 설정
            item.setIsFavorite(finalFavoritedComIds.contains(comId));
        });

        return dto;
    }

    public AccommodationOneDto findById(Long comId, LocalDate checkIn, LocalDate checkOut){
        AccommodationEntity accommodation = accommodationRepository.findById(comId)
                .orElseThrow(() -> new RuntimeException("숙소를 찾을 수 없습니다."));


        Integer price = accommodationRepository.findLowestPriceByHotelId(comId);

        // [--- ❌ 기존 쿼리 방식 ❌ ---]
        // String mainImage = accommodationRepository.findMainComImage(comId);
        // List<String> subImage = accommodationRepository.findSubComImage(comId);

        // [--- ✅ 수정된 로직 (Java 스트림 방식) ✅ ---]
        // 1. com_id로 모든 이미지 엔티티를 한 번에 가져옵니다.
        List<ComImageEntity> allImages = comImageRepository.findByAccommodation_ComId(comId);

        // 2. Java 스트림으로 메인 이미지를 찾습니다. (is_main = 1)
        String mainImage = allImages.stream()
                .filter(ComImageEntity::getIsMain) // isMain == true
                .map(ComImageEntity::getImageUrl)
                .findFirst()
                .orElse(null);

        // 3. Java 스트림으로 서브 이미지 리스트를 만듭니다. (is_main = 0)
        List<String> subImage = allImages.stream()
                .filter(image -> !image.getIsMain()) // isMain == false
                .map(ComImageEntity::getImageUrl)
                .collect(Collectors.toList());
        // [--- ✅ 수정 끝 ✅ ---]


        AccommodationOneDto dto = AccommodationOneDto.fromEntity(accommodation);

        // ----------------------------------------------------
        // [✅ 1단계: 사용할 객실 목록 초기화]
        // 기본적으로 전체 객실 목록을 사용합니다.
        List<AccRoomTypeDto> roomsToProcess = dto.getRooms();

        // ----------------------------------------------------
        // [✅ 2단계: 체크인/체크아웃 정보가 있으면 잔여 객실로 갱신]
        if (checkIn != null && checkOut != null) {
            // DB에서 잔여 객실만 조회
            List<AccRoomTypeEntity> availableEntities = accRoomTypeRepository.findAvailableRoomEntities(comId, checkIn, checkOut);

            // 잔여 객실 DTO를 새로 생성
            roomsToProcess = availableEntities.stream()
                    .map(AccRoomTypeDto::fromEntity)
                    .collect(Collectors.toList());

            // **필터링된 객실 목록으로 DTO의 rooms 필드를 즉시 갱신합니다.**
            dto.setRooms(roomsToProcess);
        }

        // ----------------------------------------------------
        // [✅ 3단계: 현재 (필터링된) 객실 목록에 할인 가격 적용]
        BigDecimal discountRate = accommodation.getDiscountRate();

        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal oneHundred = new BigDecimal("100");

            // roomsToProcess (잔여 객실 목록이거나 전체 객실 목록) 에 할인을 적용
            for (AccRoomTypeDto roomDto : roomsToProcess) {
                int originalPrice = roomDto.getPrice();

                // 할인 계산 로직은 기존과 동일
                BigDecimal priceBD = new BigDecimal(originalPrice);
                BigDecimal discountFactor = BigDecimal.ONE.subtract(discountRate.divide(oneHundred));
                BigDecimal discountedPriceBD = priceBD.multiply(discountFactor);

                roomDto.setDiscountedPrice(discountedPriceBD.intValue());
                roomDto.setDiscountRate(discountRate);
            }
        }

        // ----------------------------------------------------
        // [❌ 기존의 중복 로직 제거: 덮어쓰는 부분 제거]
    /*
    if (checkIn != null && checkOut != null) {
        // ... (기존 잔여 객실 조회 및 DTO 생성 로직) ...
        dto.setRooms(availableDtos); // <--- 이 부분이 문제였습니다.
    }
    */
        // ----------------------------------------------------

        dto.setPrice(price);
        dto.setMainImage(mainImage);
        dto.setSubImage(subImage);

        return dto;
    }

    public Page<AccommodationDiscountDto> findDiscountedAccommodations(Pageable pageable) {
        return accommodationRepository.findDiscountedHotels(BigDecimal.ZERO, pageable);
    }

    public Resource loadImage(String filename) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found " + filename);
        }
    }
}