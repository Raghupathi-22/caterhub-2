package com.daily.cetaring.features.user.controller;

import com.daily.cetaring.features.user.service.UserProfileService;
import com.daily.cetaring.shared.dto.UpdateUserProfileRequest;
import com.daily.cetaring.shared.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public UserDTO getMyProfile(Authentication authentication) {
        return userProfileService.getProfile(authentication.getName());
    }

    @PatchMapping("/me")
    public UserDTO updateMyProfile(Authentication authentication, @Valid @RequestBody UpdateUserProfileRequest request) {
        return userProfileService.updateProfile(authentication.getName(), request);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        userProfileService.deleteMyAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
