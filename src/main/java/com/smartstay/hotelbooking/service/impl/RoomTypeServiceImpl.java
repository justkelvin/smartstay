package com.smartstay.hotelbooking.service.impl;

import com.smartstay.hotelbooking.model.entity.RoomType;
import com.smartstay.hotelbooking.repository.RoomTypeRepository;
import com.smartstay.hotelbooking.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Autowired
    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Override
    public RoomType createRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Override
    public Optional<RoomType> findById(Long id) {
        return roomTypeRepository.findById(id);
    }

    @Override
    public Optional<RoomType> findByName(String name) {
        return roomTypeRepository.findByName(name);
    }

    @Override
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    @Override
    public List<RoomType> findByBasePriceLessThan(BigDecimal maxPrice) {
        return roomTypeRepository.findByBasePriceLessThan(maxPrice);
    }

    @Override
    public List<RoomType> findByBaseCapacityGreaterThanEqual(int capacity) {
        return roomTypeRepository.findByBaseCapacityGreaterThanEqual(capacity);
    }

    @Override
    public RoomType updateRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Override
    public void deleteRoomType(Long id) {
        roomTypeRepository.deleteById(id);
    }
}