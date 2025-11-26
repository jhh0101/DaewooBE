package com.example.daewoo.reservation.dto;

import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private Long reservationId;
    private String roomNumber;
    private String  comTitle;
    private Long  accId;
    private Long userId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    public ReservationDto(Long reservationId, String roomNumber, String comTitle, Long accId, Long userId, LocalTime checkInTime, LocalTime checkOutTime, LocalDate checkIn, LocalDate checkOut) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.comTitle = comTitle;
        this.accId = accId;
        this.userId = userId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
    public ReservationEntity toEntity(){
        ReservationEntity entity = new ReservationEntity();
        entity.setReservationId(this.reservationId);
        entity.setCheckIn(this.checkIn);
        entity.setCheckOut(this.checkOut);

        return entity;
    }

    public static ReservationDto fromEntity(ReservationEntity entity){
        ReservationDto dto = new ReservationDto();
        dto.setReservationId(entity.getReservationId());
        dto.setCheckIn(entity.getCheckIn());
        dto.setCheckOut(entity.getCheckOut());
        dto.setRoomNumber(entity.getParlorEntity().getParContent());
        dto.setComTitle(entity.getParlorEntity().getAccRoomTypeEntity().getAccommodation().getComTitle());
        dto.setAccId(entity.getParlorEntity().getAccRoomTypeEntity().getAccId());
        dto.setUserId(entity.getUserEntity().getUserId());
        dto.setCheckInTime(entity.getParlorEntity().getAccRoomTypeEntity().getAccommodation().getCheckInTime());
        dto.setCheckOutTime(entity.getParlorEntity().getAccRoomTypeEntity().getAccommodation().getCheckOutTime());

        return dto;
    }
}
