package com.example.daewoo.common.toss.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PriceCalculationRequest {
    private Long accId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}
