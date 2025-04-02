package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.RoomType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RoomTypeService {
    RoomType createRoomType(RoomType roomType);

    Optional<RoomType> findById(Long id);

    Optional<RoomType> findByName(String name);

    List<RoomType> findAll();

    List<RoomType> findByBasePriceLessThan(BigDecimal maxPrice);

    List<RoomType> findByBaseCapacityGreaterThanEqual(int capacity);

    RoomType updateRoomType(RoomType roomType);

    void deleteRoomType(Long id);
}