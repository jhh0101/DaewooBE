package com.example.daewoo.reservation.service;

import com.example.daewoo.common.toss.dto.TossPaymentDto;
import com.example.daewoo.parlor.dto.ParlorEntity;
import com.example.daewoo.parlor.service.AccRoomTypeRepository;
import com.example.daewoo.parlor.service.ParlorRepository;
import com.example.daewoo.reservation.dto.ReservationDto;
import com.example.daewoo.reservation.dto.ReservationEntity;
import com.example.daewoo.user.dto.UserEntity;
import com.example.daewoo.user.service.UserRepository;
import com.example.daewoo.wish.dto.WishDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParlorRepository parlorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccRoomTypeRepository accRoomTypeRepository;

    public void insert(Long userId, TossPaymentDto dto){

        ReservationEntity entity = dto.toEntity();

        ParlorEntity parlorEntity = resolveParlorForReservation(dto.getAccId(), dto.getCheckIn(), dto.getCheckOut());
        entity.setParlorEntity(parlorEntity);

        UserEntity userEntity = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User Not Found"));
        entity.setUserEntity(userEntity);

        this.reservationRepository.save(entity);
    }


    public List<ReservationDto> findByUserId(Long userId){
        if (userId == null) {
            // userId가 null인 경우 처리
            return Collections.emptyList();
        }

        // 2. Repository 메서드 호출
        // JPQL 쿼리 덕분에, DB에서 이미 checkInTime과 checkOutTime이 채워진 DTO 리스트를 바로 가져옵니다.
        List<ReservationDto> reservations =
                reservationRepository.findAllReservationsByUserIdWithCheckInOut(userId);

        // 3. 결과 반환
        // 별도의 DTO 변환 과정 없이 바로 반환 가능 (Projection의 장점)
        return reservations;

    }


    public void update(Long userId, TossPaymentDto dto){
        ReservationEntity entity = dto.toEntity();

        ParlorEntity parlorEntity = resolveParlorForReservation(dto.getAccId(), dto.getCheckIn(), dto.getCheckOut());
        entity.setParlorEntity(parlorEntity);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        entity.setUserEntity(userEntity);

        this.reservationRepository.save(entity);
    }
    private ParlorEntity resolveParlorForReservation(Long accId, LocalDate checkIn, LocalDate checkOut) {

        // 1. 필수값 체크
        if (accId == null || checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("필수 정보 누락");
        }

        // 2. 방 목록 조회
        List<ParlorEntity> parlors = parlorRepository.findByAccRoomTypeEntityAccId(accId);
        if (parlors.isEmpty()) {
            throw new IllegalStateException("예약 가능한 객실이 없습니다.");
        }

        // 3. 빈 방 찾기 (로직은 그대로)
        for (ParlorEntity candidate : parlors) {
            boolean overlapping = reservationRepository
                    .existsByParlorEntityParIdAndCheckOutGreaterThanEqualAndCheckInLessThanEqual(
                            candidate.getParId(), checkIn, checkOut); // dto.get... 대신 변수 사용
            if (overlapping) continue;

            return candidate; // 찾은 방(Entity) 바로 리턴
        }

        throw new IllegalStateException("남은 방이 없습니다.");
    }

    public void delete(Long id){
        this.reservationRepository.deleteById(id);
    }

    public Integer findPriceByAccId(Long accId) {
        return accRoomTypeRepository.findPriceByAccId(accId);
    }

    /**
     * AccRoomType ID로 해당 숙소(Accommodation) 엔티티 조회
     */
    public com.example.daewoo.accommodation.dto.AccommodationEntity findAccommodationByAccId(Long accId) {
        return accRoomTypeRepository.findById(accId)
                .map(accRoomType -> accRoomType.getAccommodation())
                .orElse(null);
    }
}
