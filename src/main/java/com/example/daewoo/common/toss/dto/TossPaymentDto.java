package com.example.daewoo.common.toss.dto;

import com.example.daewoo.reservation.dto.ReservationEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TossPaymentDto {
    private String orderId;
    private Double amount;
    private String paymentKey;
    private String customerKey;

    private Long reservationId;
    private Long userId;
    private Long accId;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public ReservationEntity toEntity(){
        ReservationEntity entity = new ReservationEntity();

        entity.setCheckIn(this.checkIn);
        entity.setCheckOut(this.checkOut);
        entity.setOrderId(this.orderId);
        entity.setAmount(this.amount);
        entity.setPaymentKey(this.paymentKey);

        return entity;
    }
}