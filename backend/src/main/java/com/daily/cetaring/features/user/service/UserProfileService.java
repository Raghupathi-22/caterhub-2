package com.daily.cetaring.features.user.service;

import com.daily.cetaring.features.auth.mapper.UserMapper;
import com.daily.cetaring.features.auth.service.MobileNumberNormalizer;
import com.daily.cetaring.shared.dto.UpdateUserProfileRequest;
import com.daily.cetaring.shared.dto.UserDTO;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RefreshTokenRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDTO getProfile(String username) {
        return userRepository.findByUsername(username)
            .map(userMapper::toDTO)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    public UserDTO updateProfile(String username, UpdateUserProfileRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        String email = trimToNull(request.getEmail());
        if (email != null && !email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        String phone = trimToNull(request.getPhoneNumber());
        if (phone != null && !phone.equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        user.setFirstName(trimToNull(request.getFirstName()));
        user.setLastName(trimToNull(request.getLastName()));
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhoneNumber(phone);

        return userMapper.toDTO(userRepository.save(user));
    }

    public void deleteMyAccount(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        deleteCustomerAccount(user);
    }

    public void deleteCustomerAccountByMobile(String mobileNumber) {
        String normalized = MobileNumberNormalizer.normalize(mobileNumber);
        User user = findByMobileNumber(normalized)
            .orElseThrow(() -> new IllegalArgumentException("No account exists for this mobile number."));
        deleteCustomerAccount(user);
    }

    private java.util.Optional<User> findByMobileNumber(String normalizedMobile) {
        java.util.Optional<User> directMatch = userRepository.findByPhoneNumber(normalizedMobile);
        if (directMatch.isPresent()) {
            return directMatch;
        }
        if (normalizedMobile.startsWith("+91") && normalizedMobile.length() == 13) {
            return userRepository.findByPhoneNumber(normalizedMobile.substring(3));
        }
        return java.util.Optional.empty();
    }

    private void deleteCustomerAccount(User user) {
        ensureCustomerDeletionEligible(user);

        LocalDateTime now = LocalDateTime.now();
        Long userId = user.getId();
        String suffix = "deleted-" + userId + "-" + System.currentTimeMillis();

        refreshTokenRepository.revokeAllActiveTokensForUser(user, now);
        anonymizeNotificationData(userId);

        user.setIsActive(false);
        user.setDeletedAt(now);
        user.setUsername(suffix);
        user.setEmail(suffix + "@deleted.caterhub.local");
        user.setPhoneNumber("+000" + userId);
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setProfileImageUrl(null);
        user.setEmailVerifiedAt(null);
        user.setPhoneVerifiedAt(null);
        user.setLastLoginAt(null);
        user.setBusiness(null);
        userRepository.save(user);
    }

    private void ensureCustomerDeletionEligible(User user) {
        java.util.List<String> roles = roleNames(user);
        boolean isAdmin = roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN");
        if (isAdmin) {
            throw new IllegalArgumentException("Admin accounts cannot be deleted from this customer flow.");
        }
        boolean isWorker = roles.contains("ROLE_WORKER");
        if (isWorker) {
            throw new IllegalArgumentException("Worker/partner accounts use a separate support-assisted deletion process.");
        }
        boolean isCustomer = roles.contains("ROLE_CUSTOMER");
        if (!isCustomer) {
            throw new IllegalArgumentException("Only customer accounts can be deleted from this flow.");
        }
    }

    private void anonymizeNotificationData(Long userId) {
        entityManager.createQuery("delete from FcmToken token where token.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
        entityManager.createQuery("delete from NotificationPreference preference where preference.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
        entityManager.createQuery("""
                update Notification notification
                set notification.recipientAddress = null,
                    notification.payload = null,
                    notification.status = com.daily.cetaring.features.notification.entity.Notification.NotificationStatus.CANCELLED
                where notification.userId = :userId
                  and notification.status = com.daily.cetaring.features.notification.entity.Notification.NotificationStatus.PENDING
                """)
            .setParameter("userId", userId)
            .executeUpdate();
    }

    public static java.util.List<String> roleNames(User user) {
        if (user.getRoles() == null) return java.util.List.of();
        return user.getRoles().stream().map(Role::getName).sorted().toList();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
