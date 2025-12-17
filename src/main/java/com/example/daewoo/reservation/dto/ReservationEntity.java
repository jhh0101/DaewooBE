package com.example.daewoo.reservation.dto;

import com.example.daewoo.parlor.dto.ParlorEntity;
import com.example.daewoo.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservation")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    // --- [1] 결제 추적용 (토스 필수) ---
    private String orderId;     // 주문번호 (우리가 만든 것, UUID)
    private String paymentKey;  // 결제키 (토스가 준 것, 환불용)
    // --- [2] 금액 기록용 (역사 기록) ---
    private Long amount;        // 실제 결제된 금액 (가격 변동 대비)

    @ManyToOne
    @JoinColumn(name = "par_id")
    private ParlorEntity parlorEntity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    private LocalDate checkIn;
    private LocalDate checkOut;


}
