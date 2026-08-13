package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.config.security.JwtTokenProvider;
import com.daily.cetaring.features.auth.mapper.UserMapper;
import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.shared.dto.LoginRequest;
import com.daily.cetaring.shared.dto.RegisterRequest;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RefreshTokenRepository;
import com.daily.cetaring.shared.repository.RoleRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Role customerRole;
    private Role workerRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("9876543210")
                .password("TestPassword123")
                .firstName("Test")
                .lastName("User")
                .build();

        loginRequest = LoginRequest.builder()
                .emailOrUsername("testuser")
                .password("TestPassword123")
                .build();

        customerRole = Role.builder()
                .id(1L)
                .name("ROLE_CUSTOMER")
                .description("Customer role")
                .build();

        workerRole = Role.builder()
                .id(3L)
                .name("ROLE_WORKER")
                .description("Worker role")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("9876543210")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .isVerified(false)
                .roles(new HashSet<>())
                .build();
        testUser.getRoles().add(customerRole);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(900000L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenRepository, times(1)).save(any());
        verify(roleRepository, times(1)).findByName("ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterFailsWhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterFailsWhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login user successfully")
    void testLoginSuccess() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(loginRequest.getEmailOrUsername(), loginRequest.getPassword()));
        when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(900000L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        verify(userRepository, times(1)).findByEmailOrUsername(anyString(), anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should register worker user with worker role")
    void testRegisterWorkerUsesWorkerRole() {
        RegisterRequest workerRequest = RegisterRequest.builder()
                .username("workeruser")
                .email("worker@example.com")
                .phoneNumber("9876543211")
                .password("TestPassword123")
                .firstName("Worker")
                .lastName("User")
                .userType("WORKER")
                .build();
        User workerUser = User.builder()
                .id(2L)
                .username("workeruser")
                .email("worker@example.com")
                .phoneNumber("9876543211")
                .passwordHash("hashedPassword")
                .firstName("Worker")
                .lastName("User")
                .isActive(true)
                .isVerified(false)
                .roles(new HashSet<>())
                .build();
        workerUser.getRoles().add(workerRole);

        when(userRepository.existsByUsername(workerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(workerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(workerRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(workerRequest.getPassword())).thenReturn("hashedPassword");
        when(roleRepository.findByName("ROLE_WORKER")).thenReturn(Optional.of(workerRole));
        when(userRepository.save(any(User.class))).thenReturn(workerUser);
        when(jwtTokenProvider.generateToken(any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(900000L);

        // Act
        AuthResponse response = authService.register(workerRequest);

        // Assert
        assertNotNull(response);
        verify(roleRepository, times(1)).findByName("ROLE_WORKER");
        verify(userRepository, times(1)).save(argThat(user ->
                user.getRoles().stream().anyMatch(role -> "ROLE_WORKER".equals(role.getName()))
        ));
    }
}
