package com.example.daewoo.common.toss.controller;

import com.example.daewoo.common.toss.dto.TossPaymentDto;
import com.example.daewoo.common.toss.service.TossPaymentsService;
import com.example.daewoo.reservation.service.ReservationService;
import com.example.daewoo.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/payment")
public class TossPaymentsController {

    private final TossPaymentsService tossPaymentsService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserService userService;


    public TossPaymentsController(TossPaymentsService tossPaymentsService) {
        this.tossPaymentsService = tossPaymentsService;
    }

    @PostMapping("/toss/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody TossPaymentDto tossPaymentDto,
                                            Authentication authentication) {
        try {
            Long days = ChronoUnit.DAYS.between(tossPaymentDto.getCheckIn(), tossPaymentDto.getCheckOut());
            if (days < 1) {
                days = 1L;
            } // 최소 1박
            Long price = Long.valueOf(reservationService.findPriceByAccId(tossPaymentDto.getAccId())) * days;
            if (!price.equals(tossPaymentDto.getAmount())){
                return ResponseEntity.badRequest().body("금액이 일치하지 않습니다.");
            }
            // TODO: 성공 시, DB에 주문 정보 저장 등의 로직을 여기에 추가하세요.
            Long userId = userService.findByEmail(authentication.getName()).getUserId();
            tossPaymentDto.setUserId(userId);
            JsonNode payment = tossPaymentsService.confirmPayment(tossPaymentDto);
            this.reservationService.insert(userId ,tossPaymentDto);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            // TODO: 실패 시, 에러 처리 로직을 여기에 추가하세요.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}