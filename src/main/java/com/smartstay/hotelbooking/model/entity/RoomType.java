package com.smartstay.hotelbooking.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartstay.hotelbooking.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_capacity", nullable = false)
    private Integer baseCapacity;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String amenities;

    @JsonIgnore
    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL)
    private List<Room> rooms = new ArrayList<>();
}