package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.repository.UserRepository;
import com.smartstay.hotelbooking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.UserRole.CUSTOMER);
    }

    @Test
    void registerUser_shouldEncodePasswordAndSaveUser() {
        // Arrange
        User inputUser = new User();
        inputUser.setUsername("newuser");
        inputUser.setPassword("password123");
        inputUser.setEmail("new@example.com");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser(inputUser);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(inputUser);
        assertEquals(testUser, result);
    }

    @Test
    void findByUsername_shouldReturnUser_whenUserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername("nonexistentuser");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findAllUsers_shouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
    }

    @Test
    void deleteUser_shouldCallRepositoryDeleteById() {
        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }
}