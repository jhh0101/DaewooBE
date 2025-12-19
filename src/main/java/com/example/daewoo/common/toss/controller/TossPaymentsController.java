package com.example.daewoo.common.toss.controller;

import com.example.daewoo.accommodation.dto.AccommodationEntity;
import com.example.daewoo.accommodation.service.AccommodationRepository;
import com.example.daewoo.common.toss.dto.PriceCalculationRequest;
import com.example.daewoo.common.toss.dto.PriceCalculationResponse;
import com.example.daewoo.common.toss.dto.TossPaymentDto;
import com.example.daewoo.common.toss.service.TossPaymentsService;
import com.example.daewoo.reservation.service.ReservationService;
import com.example.daewoo.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

    private static final BigDecimal TAX_RATE = new BigDecimal("0.1");
    private static final Long SERVICE_FEE = 5000L;

    public TossPaymentsController(TossPaymentsService tossPaymentsService) {
        this.tossPaymentsService = tossPaymentsService;
    }

    /**
     * 가격 미리보기 API - 프론트엔드에서 결제 전 금액 확인용
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<?> calculatePrice(@RequestBody PriceCalculationRequest request) {
        try {
            PriceCalculationResponse response = calculatePriceInternal(
                    request.getAccId(),
                    request.getCheckIn(),
                    request.getCheckOut()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 가격 계산 내부 로직 (재사용 가능)
     */
    private PriceCalculationResponse calculatePriceInternal(Long accId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("체크인/체크아웃 날짜가 필요합니다.");
        }

        // 숙박 일수 계산 (최소 1박)
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days < 1) {
            days = 1L;
        }

        // 기본 가격 조회
        Integer basePriceInt = reservationService.findPriceByAccId(accId);
        if (basePriceInt == null) {
            throw new IllegalArgumentException("가격 정보를 찾을 수 없습니다.");
        }

        // AccRoomType에서 Accommodation을 찾아서 할인율 조회
        AccommodationEntity accommodation = reservationService.findAccommodationByAccId(accId);
        BigDecimal discountRate = BigDecimal.ZERO;
        if (accommodation != null && accommodation.getDiscountRate() != null) {
            discountRate = accommodation.getDiscountRate();
        }

        // BigDecimal로 정확한 계산
        BigDecimal basePrice = new BigDecimal(basePriceInt.toString());
        BigDecimal totalBasePrice = basePrice.multiply(BigDecimal.valueOf(days));

        // 할인 금액 계산
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = totalBasePrice
                    .multiply(discountRate)
                    .divide(new BigDecimal("100"), 0, RoundingMode.DOWN);
        }

        // 할인 적용 후 금액
        BigDecimal priceAfterDiscount = totalBasePrice.subtract(discountAmount);

        // 세금 계산 (원금의 10%)
        BigDecimal taxAmount = totalBasePrice.multiply(TAX_RATE).setScale(0, RoundingMode.DOWN);

        // 서비스 요금
        BigDecimal serviceFee = new BigDecimal(SERVICE_FEE);

        // 최종 가격
        BigDecimal finalPrice = priceAfterDiscount.add(taxAmount).add(serviceFee);

        log.info("가격 계산 - accId: {}, {}박, 기본가: {}, 할인율: {}%, 할인액: {}, 세금: {}, 서비스비: {}, 최종: {}",
                accId, days, totalBasePrice, discountRate, discountAmount, taxAmount, serviceFee, finalPrice);

        return PriceCalculationResponse.builder()
                .days(days)
                .basePrice(basePriceInt.longValue())
                .totalBasePrice(totalBasePrice.longValue())
                .discountRate(discountRate.longValue())
                .discountAmount(discountAmount.longValue())
                .priceAfterDiscount(priceAfterDiscount.longValue())
                .taxAmount(taxAmount.longValue())
                .serviceFee(SERVICE_FEE)
                .finalPrice(finalPrice.longValue())
                .build();
    }

    @PostMapping("/toss/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody TossPaymentDto tossPaymentDto,
                                            Authentication authentication,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("결제 요청 데이터: {}", tossPaymentDto);
            log.info("Authorization 헤더: {}", authHeader);
            log.info("Authentication 객체: {}", authentication);

            // 인증 확인
            if (authentication == null) {
                log.error("Authentication is null. Authorization header: {}", authHeader);
                return ResponseEntity.status(401).body("로그인이 필요합니다. (인증 정보 없음)");
            }

            // 가격 계산
            PriceCalculationResponse priceInfo = calculatePriceInternal(
                    tossPaymentDto.getAccId(),
                    tossPaymentDto.getCheckIn(),
                    tossPaymentDto.getCheckOut()
            );

            Long calculatedPrice = priceInfo.getFinalPrice();
            Long requestAmount = tossPaymentDto.getAmount();

            // 금액 검증
            if (requestAmount == null) {
                return ResponseEntity.badRequest().body("결제 금액이 필요합니다.");
            }

            // 금액 비교 (1원 이내 차이는 허용)
            if (Math.abs(calculatedPrice - requestAmount) > 1) {
                log.warn("금액 불일치 - 계산된 금액: {}, 요청 금액: {}", calculatedPrice, requestAmount);
                return ResponseEntity.badRequest().body(
                        "금액이 일치하지 않습니다. 계산된 금액: " + calculatedPrice + "원, 요청 금액: " + requestAmount + "원"
                );
            }

            log.info("금액 검증 성공 - 계산된 금액: {}원, 요청 금액: {}원", calculatedPrice, requestAmount);

            // 결제 처리
            Long userId = userService.findByEmail(authentication.getName()).getUserId();
            tossPaymentDto.setUserId(userId);
            tossPaymentDto.setReservationId(null);
            JsonNode payment = tossPaymentsService.confirmPayment(tossPaymentDto);
            this.reservationService.insert(userId, tossPaymentDto);

            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}