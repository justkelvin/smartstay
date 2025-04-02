package com.smartstay.hotelbooking.repository;

import com.smartstay.hotelbooking.model.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    Optional<RoomType> findByName(String name);

    List<RoomType> findByBasePriceLessThan(BigDecimal maxPrice);

    List<RoomType> findByBaseCapacityGreaterThanEqual(int capacity);
}