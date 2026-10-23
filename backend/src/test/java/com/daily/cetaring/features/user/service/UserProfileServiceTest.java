package com.daily.cetaring.features.user.service;

import com.daily.cetaring.features.auth.mapper.UserMapper;
import com.daily.cetaring.shared.dto.UpdateUserProfileRequest;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RefreshTokenRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EntityManager entityManager;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserProfileService userProfileService;

    private User existingUser() {
        User user = new User();
        user.setUsername("asha");
        user.setPhoneNumber("+919876543210");
        user.setEmail("old@example.com");
        return user;
    }

    @Test
    void changingVerifiedPhoneNumberIsRejected() {
        User user = existingUser();
        when(userRepository.findByUsername("asha")).thenReturn(Optional.of(user));

        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
            .firstName("Asha")
            .phoneNumber("+919999999999")
            .build();

        assertThrows(IllegalArgumentException.class, () -> userProfileService.updateProfile("asha", request));
        verify(userRepository, never()).save(any());
        assertEquals("+919876543210", user.getPhoneNumber());
    }

    @Test
    void unchangedOrMissingPhoneAllowsProfileUpdate() {
        User user = existingUser();
        when(userRepository.findByUsername("asha")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.updateProfile("asha", UpdateUserProfileRequest.builder()
            .firstName(" Asha ")
            .lastName("Rao")
            .phoneNumber("+919876543210")
            .build());
        userProfileService.updateProfile("asha", UpdateUserProfileRequest.builder()
            .firstName("Asha")
            .build());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(saved.capture());
        assertEquals("Asha", saved.getValue().getFirstName());
        assertEquals("+919876543210", saved.getValue().getPhoneNumber());
    }
}
