package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.config.security.JwtTokenProvider;
import com.daily.cetaring.features.auth.dto.OtpPurpose;
import com.daily.cetaring.features.auth.mapper.UserMapper;
import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RefreshTokenRepository;
import com.daily.cetaring.shared.repository.RoleRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserMapper userMapper;
    @InjectMocks private AuthService authService;

    @Test
    void registersWorkerOnlyForWorkerRegistrationPurpose() {
        Role workerRole = Role.builder().id(3L).name("ROLE_WORKER").build();
        User savedWorker = User.builder()
                .id(2L).username("user_919876543210").phoneNumber("+919876543210")
                .firstName("Worker").isActive(true).isVerified(true).roles(new HashSet<>()).build();
        savedWorker.getRoles().add(workerRole);
        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_WORKER")).thenReturn(Optional.of(workerRole));
        when(userRepository.save(any(User.class))).thenReturn(savedWorker);
        when(jwtTokenProvider.generateToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(900_000L);

        AuthResponse response = authService.authenticateWithVerifiedOtp(
                "9876543210", "Worker One", OtpPurpose.REGISTER_WORKER);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void loginDoesNotCreateAnAccountForAnUnknownMobileNumber() {
        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                authService.authenticateWithVerifiedOtp("9876543210", null, OtpPurpose.LOGIN));
    }
}
