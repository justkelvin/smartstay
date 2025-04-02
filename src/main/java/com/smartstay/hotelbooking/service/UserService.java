package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User updateUser(User user);

    List<User> findAllUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void deleteUser(Long id);
}