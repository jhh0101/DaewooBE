package com.example.daewoo.reservation.service;

import com.example.daewoo.reservation.dto.ReservationDto;
import com.example.daewoo.reservation.dto.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @Query("SELECT new com.example.daewoo.reservation.dto.ReservationDto(" +
            "    r.reservationId," +
            "    r.parlorEntity.parContent, " +
            "    r.parlorEntity.accRoomTypeEntity.accommodation.comTitle, " +
            "    r.parlorEntity.accRoomTypeEntity.accId, " +
            "    r.userEntity.userId," +
            "    r.parlorEntity.accRoomTypeEntity.accommodation.checkInTime," +
            "    r.parlorEntity.accRoomTypeEntity.accommodation.checkOutTime," +
            "    r.checkIn," +
            "    r.checkOut) " +
            "FROM ReservationEntity r " +
            "WHERE r.userEntity.userId = :userId " + // ✨ userId를 통한 조건 추가
            "ORDER BY r.reservationId DESC") // 필요에 따라 정렬 조건 추가
    List<ReservationDto> findAllReservationsByUserIdWithCheckInOut(@Param("userId") Long userId);

//    boolean existsByParlorEntityParIdAndCheckInEqualsAndCheckOutEquals(Long parId, LocalDate checkIn, LocalDate checkOut);

    boolean existsByParlorEntityParIdAndCheckOutGreaterThanEqualAndCheckInLessThanEqual(Long parId, LocalDate checkIn, LocalDate checkOut);

    @Query("SELECT r.parlorEntity.accRoomTypeEntity.price FROM ReservationEntity r WHERE r.parlorEntity.accRoomTypeEntity.accId = :accId")
    Integer findPriceByAccId(@Param("accId") Long accId);
}
