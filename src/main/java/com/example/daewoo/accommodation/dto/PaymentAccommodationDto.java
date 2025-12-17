package com.example.daewoo.accommodation.dto;

import com.example.daewoo.accommodation.location.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAccommodationDto {
    private Long comId;
    private Long accId;  // 프론트엔드 호환용 (comId와 동일)
    private String comTitle;
    private String comAddress;
    private Integer price;
    private BigDecimal reviewAvg;
    private Integer reviewCount;
    private String mainImage;
    private Integer discount;

    private String location;

    public AccommodationEntity toEntity(){
        AccommodationEntity entity = new AccommodationEntity();
        entity.setComId(this.comId);
        entity.setComTitle(this.comTitle);
        entity.setComAddress(this.comAddress);
        entity.setReviewAvg(this.reviewAvg);
        entity.setReviewCount(this.reviewCount);

        return entity;
    }

    public static PaymentAccommodationDto fromEntity(AccommodationEntity entity, Integer price, String mainImage){
        PaymentAccommodationDto dto = new PaymentAccommodationDto();
        dto.setComId(entity.getComId());
        dto.setAccId(entity.getComId());  // accId = comId (프론트엔드 호환)
        dto.setComTitle(entity.getComTitle());
        dto.setComAddress(entity.getComAddress());
        dto.setReviewAvg(entity.getReviewAvg());
        dto.setReviewCount(entity.getReviewCount());
        dto.setMainImage(mainImage);
        dto.setPrice(price);
        dto.setLocation(entity.getLocationEntity().getLocationName());

        BigDecimal bdPrice = new BigDecimal(price);
        BigDecimal discountRate = entity.getDiscountRate();

        BigDecimal hundred = new BigDecimal("100");
        BigDecimal rateFraction = discountRate.divide(hundred, 4, RoundingMode.HALF_UP);

        BigDecimal bdDiscount = bdPrice.multiply(rateFraction)
                .setScale(0, RoundingMode.HALF_UP);

        Integer discount = bdDiscount.intValue();

        dto.setDiscount(discount);

        return dto;
    }
}
