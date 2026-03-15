package com.example.daewoo.common.toss.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PriceCalculationResponse {
    private Long days;              // 숙박 일수
    private Long basePrice;         // 1박 가격
    private Long totalBasePrice;    // 총 기본 요금 (1박 * 일수)
    private Long discountRate;      // 할인율 (%)
    private Long discountAmount;    // 할인 금액
    private Long priceAfterDiscount;// 할인 적용 후 금액
    private Long taxAmount;         // 세금 (원금의 10%)
    private Long serviceFee;        // 서비스 요금 (고정 5,000원)
    private Long finalPrice;        // 최종 결제 금액
}
