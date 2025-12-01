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
            
            // 1. 기본 가격 (1박당)
            double basePricePerNight = basePrice.doubleValue();
            log.info("1. 기본 가격 (1박당): {}원", basePricePerNight);
            
            // 2. 세금 계산 (원금의 10%)
            double tax = basePricePerNight * 0.1;
            log.info("2. 세금 (10%): {}원", tax);
            
            // 3. 할인 금액 계산 (원금의 할인율%)
            double discountAmount = 0;
            if (discountRate != null && discountRate.doubleValue() > 0) {
                discountAmount = basePricePerNight * (discountRate.doubleValue() / 100);
                log.info("3. 할인 적용 ({}%): -{}원", discountRate, discountAmount);
            } else {
                log.info("3. 할인 미적용");
            }
            
            // 4. 1박당 최종 가격 (원금 + 세금 - 할인)
            double finalPricePerNight = basePricePerNight + tax - discountAmount;
            log.info("4. 1박당 최종 가격: {}원 (원금: {}, 세금: {}, 할인: -{})", 
                    finalPricePerNight, basePricePerNight, tax, discountAmount);
            
            // 5. 숙박 일수 적용 및 수수료 5,000원 추가
            double calculatedPrice = (finalPricePerNight * days) + 5000;
            log.info("4. 최종 계산 ({}박, 수수료 포함): {}원 (반올림 전)", days, calculatedPrice);
            // 소수점 이하 반올림
            long price = Math.round(calculatedPrice);
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