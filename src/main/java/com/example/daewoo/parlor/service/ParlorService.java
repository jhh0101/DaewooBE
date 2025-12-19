package com.example.daewoo.parlor.service;

import com.example.daewoo.accommodation.dto.PaymentAccommodationDto;
import com.example.daewoo.accommodation.service.AccommodationRepository;
import com.example.daewoo.parlor.dto.ParlorDto;
import com.example.daewoo.parlor.dto.ParlorEntity;
import com.example.daewoo.parlor.roomtype.AccRoomTypeEntity;
import com.example.daewoo.parlor.roomtype.PaymentAccRoomTypeDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParlorService {
    @Autowired
    private ParlorRepository parlorRepository;

    @Autowired
    private AccRoomTypeRepository accRoomTypeRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;


    public void insert(ParlorDto dto){
        ParlorEntity entity = dto.toEntity();

        AccRoomTypeEntity accRoomType = accRoomTypeRepository.findById(dto.getAccId())
                .orElseThrow(() -> new RuntimeException("RoomType Not Found"));
        entity.setAccRoomTypeEntity(accRoomType);

        this.parlorRepository.save(entity);

        AccRoomTypeEntity accRoomTypeEntity = accRoomTypeRepository.findById(dto.getAccId()).orElseThrow();
        Integer maxRoom = this.parlorRepository.countMaxRoomByParlor(dto.getAccId());
        accRoomTypeEntity.setMaxRoom(maxRoom);
        accRoomTypeRepository.save(accRoomTypeEntity);
    }

    public PaymentAccRoomTypeDto findById(Long accId) {
        // 1. AccRoomTypeEntity 조회 (accId 사용)
        AccRoomTypeEntity accRoomTypeEntity = accRoomTypeRepository.findById(accId)
                .orElseThrow(() -> new EntityNotFoundException("객실 옵션을 찾을 수 없습니다. accId: " + accId));

        Long comId = accRoomTypeEntity.getAccommodation().getComId();

        Integer price = accRoomTypeRepository.findPriceByAccId(accId);
        String mainImage = accommodationRepository.findMainComImage(comId);

        PaymentAccommodationDto comDto = PaymentAccommodationDto.fromEntity(
                accRoomTypeEntity.getAccommodation(),
                price,
                mainImage,
                accId  // 실제 AccRoomType의 accId 전달
        );

        return PaymentAccRoomTypeDto.fromEntity(accRoomTypeEntity, comDto);
    }

}
