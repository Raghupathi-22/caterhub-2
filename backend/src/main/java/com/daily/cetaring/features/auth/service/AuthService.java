package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.features.auth.dto.OtpPurpose;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticateWithVerifiedOtp(String mobileNumber, String name, OtpPurpose purpose) {
        if (purpose == OtpPurpose.CUSTOMER_ACCESS) {
            // CUSTOMER_ACCESS must go through CustomerAccessService so that accounts are only created
            // with a server-issued registration ticket.
            throw new IllegalArgumentException("Unsupported verification request. Please update the app and try again.");
        }
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        Optional<User> existingUserOpt = findByMobileNumber(normalizedMobile);

        User user;
        if (purpose == OtpPurpose.LOGIN) {
            user = existingUserOpt.orElseThrow(() ->
                    new IllegalArgumentException("No account exists for this mobile number. Please register first."));
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.save(user);
            log.info("Existing user authenticated via OTP: {} (ID: {})", com.daily.cetaring.shared.util.LogMasking.maskPhone(user.getPhoneNumber()), user.getId());
        } else {
            if (existingUserOpt.isPresent()) {
                throw new IllegalArgumentException("An account already exists for this mobile number. Please login.");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Full name is required to register");
            }
            String roleName = purpose == OtpPurpose.REGISTER_WORKER ? "ROLE_WORKER" : "ROLE_CUSTOMER";
            user = createVerifiedUser(normalizedMobile, name, roleName);
        }

        return generateAuthResponse(user);
    }

    /**
     * Creates an account for a mobile number whose ownership the server has already verified.
     * Callers must have checked that no account exists for the number; the unique phone/username
     * constraints reject concurrent duplicates.
     */
    public User createVerifiedUser(String normalizedMobile, String fullName, String roleName) {
        String[] parts = fullName.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        String username = "user_" + normalizedMobile.replaceAll("[^0-9]", "");
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(username)
                .phoneNumber(normalizedMobile)
                .firstName(firstName)
                .lastName(lastName)
                .email(null)
                .isActive(true)
                .isVerified(true)
                .phoneVerifiedAt(now)
                .lastLoginAt(now)
                .build();

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        ensureRolePresent(user, roleName);

        user = userRepository.save(user);
        log.info("New user registered via OTP: {} (Role: {})", com.daily.cetaring.shared.util.LogMasking.maskPhone(user.getPhoneNumber()), roleName);
        return user;
    }

    /** Issues access and refresh tokens for an existing user whose mobile number was just verified. */
    public AuthResponse signInVerifiedUser(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        log.info("Existing user authenticated via OTP: {} (ID: {})", com.daily.cetaring.shared.util.LogMasking.maskPhone(saved.getPhoneNumber()), saved.getId());
        return generateAuthResponse(saved);
    }

    /** Signs in a verified customer, restoring ROLE_CUSTOMER if the account has lost all its roles. */
    public AuthResponse signInVerifiedCustomer(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            ensureRolePresent(user, "ROLE_CUSTOMER");
            log.warn("Restored ROLE_CUSTOMER for role-less user ID {}", user.getId());
        }
        return signInVerifiedUser(user);
    }

    /** Issues tokens for a user created in the current transaction. */
    public AuthResponse issueTokens(User user) {
        return generateAuthResponse(user);
    }

    public Optional<User> findUserByMobile(String normalizedMobile) {
        return findByMobileNumber(normalizedMobile);
    }

    public AuthResponse authenticateAdminWithPassword(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username.trim(), password)
        );

        User user = userRepository.findByIdentifier(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
            .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_SUPER_ADMIN".equals(role.getName()));
        if (!isAdmin) {
            throw new AccessDeniedException("This account is not authorized for admin access.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user = userRepository.save(user);
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

    private String toLegacyLocalMobile(String normalizedMobile) {
        if (normalizedMobile != null && normalizedMobile.startsWith("+91") && normalizedMobile.length() == 13) {
            return normalizedMobile.substring(3);
        }
        return null;
    }
}
