package com.example.daewoo.accommodation.service;

import com.example.daewoo.accommodation.amenities.AmenitiesEntity;
import com.example.daewoo.accommodation.dto.AccommodationEntity;
import com.example.daewoo.parlor.roomtype.AccRoomTypeEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AccommodationSpecification {
    public static Specification<AccommodationEntity> hasPriceInRange(Integer minPrice, Integer maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            }

            Join<AccommodationEntity, AccRoomTypeEntity> priceJoin = root.join("rooms");

            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(priceJoin.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(priceJoin.get("price"), maxPrice));
            }

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 이하 다른 메소드는 기존과 동일합니다.
//    public static Specification<AccommodationEntity> hasAmenities(List<String> amCategory) {
//        return (root, query, criteriaBuilder) -> {
//            if (amCategory == null || amCategory.isEmpty()) {
//                return criteriaBuilder.conjunction();
//            }
//
//            Join<AccommodationEntity, AmenitiesEntity> amenitiesJoin = root.join("amenities");
//
//            Predicate categoryInPredicate = amenitiesJoin.get("amCategory").in(amCategory);
//
//            query.groupBy(root.get("id"));
//
//            query.having(criteriaBuilder.equal(criteriaBuilder.countDistinct(amenitiesJoin.get("amCategory")), (long) amCategory.size()));
//
//            return categoryInPredicate;
//        };
//    }
    public static Specification<AccommodationEntity> hasAmenities(List<String> amCategory) {
        return (root, query, criteriaBuilder) -> {
            if (amCategory == null || amCategory.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // 각 편의시설에 대해 개별적인 조인 조건을 만듭니다.
            List<Predicate> predicates = amCategory.stream().map(category -> {
                Join<AccommodationEntity, AmenitiesEntity> amenitiesJoin = root.join("amenities");
                return criteriaBuilder.equal(amenitiesJoin.get("amCategory"), category);
            }).toList();

            // 최종적으로는 모든 조인이 만족하는 숙소를 찾기 위해 AND 조건을 사용합니다.
            // 하지만 이 방식도 SQL에 따라 중복이 발생할 수 있으므로, 최종적으로는 GROUP BY/HAVING을 피하는 것이 최선입니다.

            // 🚨 최적화된 방법: Criteria API에서 'IN'과 'COUNT'를 사용하지 않고 개별 EXISTS 서브쿼리를 구현하거나,
            // 이 부분을 Querydsl 등으로 분리하는 것을 강력히 권장합니다.

            // 임시 해결책으로, 기존 코드를 유지하고 데이터베이스에 인덱스를 추가하는 것이 빠를 수 있습니다.
            // (다만 이 방법으로는 대용량 데이터 시 타임아웃 재발 가능성이 높습니다.)
            query.distinct(true); // 편의시설 조인 후 Accommodation 중복 제거.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<AccommodationEntity> hasName(String comTitle) {
        return (root, query, criteriaBuilder) -> {
            if (comTitle == null || comTitle.isBlank()) {
                return null;
            }
            return criteriaBuilder.like(root.get("comTitle"), "%" + comTitle + "%");
        };
    }

    public static Specification<AccommodationEntity> hasStar(Integer star) {
        return (root, query, criteriaBuilder) -> {
            if (star == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("star"), star);
        };
    }

    public static Specification<AccommodationEntity> hasComCategory(String comCategory) {
        return (root, query, criteriaBuilder) -> {
            if (comCategory == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("category"), comCategory);
        };
    }
}