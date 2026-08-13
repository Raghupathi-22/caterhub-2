package com.daily.cetaring.features.user.service;

import com.daily.cetaring.features.auth.mapper.UserMapper;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.features.worker.repository.WorkerProfileRepository;
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
    private final WorkerProfileRepository workerProfileRepository;
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

        LocalDateTime now = LocalDateTime.now();
        Long userId = user.getId();
        String suffix = "deleted-" + userId + "-" + System.currentTimeMillis();

        refreshTokenRepository.revokeAllActiveTokensForUser(user, now);
        anonymizeNotificationData(userId);
        retireWorkerProfile(userId, now);

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

    private void retireWorkerProfile(Long userId, LocalDateTime deletedAt) {
        workerProfileRepository.findByUserIdAndDeletedAtIsNull(userId).ifPresent(profile -> {
            profile.setStatus(WorkerProfile.WorkerStatus.SUSPENDED);
            profile.setSkills(null);
            profile.setPreferredAreas(null);
            profile.setLanguages(null);
            profile.setBio(null);
            profile.setDeletedAt(deletedAt);
            workerProfileRepository.save(profile);
        });
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
