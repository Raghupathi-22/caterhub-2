package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.shared.dto.LoginRequest;
import com.daily.cetaring.shared.dto.RegisterRequest;
import com.daily.cetaring.shared.dto.UserDTO;
import com.daily.cetaring.shared.entity.RefreshToken;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RefreshTokenRepository;
import com.daily.cetaring.shared.repository.RoleRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import com.daily.cetaring.config.security.JwtTokenProvider;
import com.daily.cetaring.features.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AuthResponse authenticateOrRegisterWithOtp(String mobileNumber, String name, String userType) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);

        Optional<User> existingUserOpt = findByMobileNumber(normalizedMobile);

        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            if (user.getRoles().isEmpty()) {
                String requestedType = userType == null ? "CUSTOMER" : userType.trim().toUpperCase(Locale.ROOT);
                String roleName = "WORKER".equals(requestedType) ? "ROLE_WORKER" : "ROLE_CUSTOMER";
                ensureRolePresent(user, roleName);
            }
            if (name != null && !name.isBlank()) {
                String[] parts = name.trim().split("\\s+", 2);
                user.setFirstName(parts[0]);
                if (parts.length > 1) {
                    user.setLastName(parts[1]);
                }
            }
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.save(user);
            log.info("Existing user authenticated via OTP: {} (ID: {})", user.getPhoneNumber(), user.getId());
        } else {
            String requestedType = userType == null ? "CUSTOMER" : userType.trim().toUpperCase(Locale.ROOT);
            String roleName = "WORKER".equals(requestedType) ? "ROLE_WORKER" : "ROLE_CUSTOMER";

            String firstName = "User";
            String lastName = "";
            if (name != null && !name.isBlank()) {
                String[] parts = name.trim().split("\\s+", 2);
                firstName = parts[0];
                if (parts.length > 1) {
                    lastName = parts[1];
                }
            }

            String username = "user_" + normalizedMobile.replaceAll("[^0-9]", "");

            user = User.builder()
                    .username(username)
                    .phoneNumber(normalizedMobile)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(null)
                    .isActive(true)
                    .isVerified(true)
                    .phoneVerifiedAt(LocalDateTime.now())
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            ensureRolePresent(user, roleName);

            user = userRepository.save(user);
            log.info("New user registered via OTP: {} (Role: {})", user.getPhoneNumber(), roleName);
        }

        return generateAuthResponse(user);
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedPhone = MobileNumberNormalizer.normalize(request.getPhoneNumber());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (mobileExists(normalizedPhone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(normalizedPhone)
                .passwordHash(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .isVerified(false)
                .build();

        // Assign role based on request
        String requestedType = request.getUserType() == null ? "CUSTOMER" : request.getUserType().trim().toUpperCase(Locale.ROOT);
        String roleName = "WORKER".equals(requestedType) ? "ROLE_WORKER" : "ROLE_CUSTOMER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);

        user = userRepository.save(user);
        log.info("New user registered: {} (ID: {})", user.getUsername(), user.getId());

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmailOrUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailOrUsername(request.getEmailOrUsername(), request.getEmailOrUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);

        log.info("User logged in: {} (ID: {})", user.getUsername(), user.getId());

        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        User user = token.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getDeletedAt() != null) {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
            throw new IllegalArgumentException("User account is inactive or deleted");
        }
        log.info("Token refreshed for user: {} (ID: {})", user.getUsername(), user.getId());

        return generateAuthResponse(user);
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);

        log.info("User logged out: {} (ID: {})", token.getUser().getUsername(), token.getUser().getId());
    }

    private AuthResponse generateAuthResponse(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getDeletedAt() != null) {
            throw new IllegalArgumentException("User account is inactive or deleted");
        }
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                .authorities(user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName()))
                        .toList())
                .build();

        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getUsername());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpirationTime() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        UserDTO userDTO = userMapper.toDTO(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime() / 1000)
                .user(userDTO)
                .build();
    }

    private void ensureRolePresent(User user, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        boolean hasRole = user.getRoles().stream().anyMatch(existingRole -> roleName.equals(existingRole.getName()));
        if (!hasRole) {
            user.getRoles().add(role);
        }
    }

    private Optional<User> findByMobileNumber(String normalizedMobile) {
        Optional<User> directMatch = userRepository.findByPhoneNumber(normalizedMobile);
        if (directMatch.isPresent()) {
            return directMatch;
        }

        String legacyMobile = toLegacyLocalMobile(normalizedMobile);
        if (legacyMobile != null) {
            return userRepository.findByPhoneNumber(legacyMobile);
        }

        return Optional.empty();
    }

    private boolean mobileExists(String normalizedMobile) {
        if (userRepository.existsByPhoneNumber(normalizedMobile)) {
            return true;
        }

        String legacyMobile = toLegacyLocalMobile(normalizedMobile);
        return legacyMobile != null && userRepository.existsByPhoneNumber(legacyMobile);
    }

    private String toLegacyLocalMobile(String normalizedMobile) {
        if (normalizedMobile != null && normalizedMobile.startsWith("+91") && normalizedMobile.length() == 13) {
            return normalizedMobile.substring(3);
        }
        return null;
    }
}
