package com.example.daewoo.common.toss.controller;

import com.example.daewoo.accommodation.dto.AccommodationEntity;
import com.example.daewoo.accommodation.service.AccommodationRepository;
import com.example.daewoo.common.toss.dto.TossPaymentDto;
import com.example.daewoo.common.toss.service.TossPaymentsService;
import com.example.daewoo.reservation.service.ReservationService;
import com.example.daewoo.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class TossPaymentsController {

    private final TossPaymentsService tossPaymentsService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccommodationRepository accommodationRepository;


    public TossPaymentsController(TossPaymentsService tossPaymentsService) {
        this.tossPaymentsService = tossPaymentsService;
    }

    @PostMapping("/toss/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody TossPaymentDto tossPaymentDto,
                                            Authentication authentication) {
        try {
            log.info("결제 요청 데이터: {}", tossPaymentDto);
            Long days = ChronoUnit.DAYS.between(tossPaymentDto.getCheckIn(), tossPaymentDto.getCheckOut());
            if (days < 1) {
                days = 1L;
            } // 최소 1박
            // 기본 가격 조회
            Integer basePrice = reservationService.findPriceByAccId(tossPaymentDto.getAccId());
            if (basePrice == null) {
                return ResponseEntity.badRequest().body("가격 정보를 찾을 수 없습니다.");
            }
            
            // 할인율 조회 (null이면 0으로 처리)
            BigDecimal discountRate = accommodationRepository.findById(tossPaymentDto.getAccId())
                    .map(AccommodationEntity::getDiscountRate)
                    .orElse(BigDecimal.ZERO);
            
            log.info("기본 가격 조회 - accId: {}, basePrice: {}원, 할인율: {}%", 
                tossPaymentDto.getAccId(), basePrice, discountRate);
            
            // 1. 총 기본 요금 계산 (1박당 가격 * 숙박 일수)
            BigDecimal totalBasePrice = new BigDecimal(basePrice.toString())
                .multiply(BigDecimal.valueOf(days));
            log.info("1. 총 기본 요금 ({}박): {}원", days, totalBasePrice);
            
            // 2. 할인 금액 계산 (총 기본 요금 * (할인율/100))
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                // 정확한 할인 금액 계산 (소수점 버림)
                discountAmount = totalBasePrice
                    .multiply(discountRate)
                    .divide(new BigDecimal("100.0"))
                    .setScale(0, java.math.RoundingMode.DOWN);
                log.info("2. 할인 적용 ({}%): -{}원", discountRate, discountAmount);
            } else {
                log.info("2. 할인 미적용");
            }
            
            // 3. 할인 적용 후 금액
            BigDecimal priceAfterDiscount = totalBasePrice.subtract(discountAmount);
            log.info("3. 할인 적용 후 금액: {}원", priceAfterDiscount);
            
            // 4. 세금 계산 (원금의 10%)
            BigDecimal taxAmount = totalBasePrice
                .multiply(new BigDecimal("0.1"))
                .setScale(0, java.math.RoundingMode.DOWN);
            log.info("4. 세금 (원금의 10%): +{}원", taxAmount);
            
            // 5. 서비스 요금 (고정 5,000원)
            BigDecimal serviceFee = new BigDecimal("5000");
            log.info("5. 서비스 요금: +{}원", serviceFee);
            
            // 6. 최종 가격 계산 (할인 적용 금액 + 세금 + 서비스 요금)
            BigDecimal finalPrice = priceAfterDiscount
                .add(taxAmount)
                .add(serviceFee);
            log.info("6. 최종 결제 금액: {}원", finalPrice);
            
            // 7. 소수점 이하 버림 처리 (내림)
            long price = finalPrice.setScale(0, java.math.RoundingMode.DOWN).longValue();
            Double requestAmount = tossPaymentDto.getAmount();

            if (tossPaymentDto.getCheckIn() == null || tossPaymentDto.getCheckOut() == null) {
                return ResponseEntity.badRequest().body("체크인/체크아웃 날짜가 필요합니다.");
            }

            // 금액 비교 (소수점 오차를 고려하여 1원 이내 차이는 허용)
            if (Math.abs(price - requestAmount) > 1) {
                log.warn("금액 불일치 - 계산된 금액: {}, 요청 금액: {}", price, requestAmount);
                return ResponseEntity.badRequest().body("금액이 일치하지 않습니다. 계산된 금액: " + price + "원, 요청 금액: " + requestAmount + "원");
            }
            // TODO: 성공 시, DB에 주문 정보 저장 등의 로직을 여기에 추가하세요.
            Long userId = userService.findByEmail(authentication.getName()).getUserId();
            tossPaymentDto.setUserId(userId);
            tossPaymentDto.setReservationId(null);
            JsonNode payment = tossPaymentsService.confirmPayment(tossPaymentDto);
            this.reservationService.insert(userId ,tossPaymentDto);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            // TODO: 실패 시, 에러 처리 로직을 여기에 추가하세요.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}