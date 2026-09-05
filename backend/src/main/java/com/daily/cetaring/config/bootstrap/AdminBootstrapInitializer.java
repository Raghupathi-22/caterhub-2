package com.daily.cetaring.config.bootstrap;

import com.daily.cetaring.features.auth.service.MobileNumberNormalizer;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.RoleRepository;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapInitializer {

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapAdminUser() {
        if (!properties.isEnabled()) {
            return;
        }
        if (properties.getUsername() == null || properties.getUsername().isBlank()) {
            throw new IllegalStateException("caterhub.bootstrap.admin.username must be set when admin bootstrap is enabled");
        }
        if (properties.getPassword() == null || properties.getPassword().isBlank()) {
            throw new IllegalStateException("caterhub.bootstrap.admin.password must be set when admin bootstrap is enabled");
        }

        String normalizedPhone = MobileNumberNormalizer.normalize(properties.getPhoneNumber());
        User admin = userRepository.findByUsername(properties.getUsername().trim())
            .or(() -> userRepository.findByPhoneNumber(normalizedPhone))
            .orElseGet(User::new);

        admin.setUsername(properties.getUsername().trim());
        admin.setEmail(properties.getEmail());
        admin.setPhoneNumber(normalizedPhone);
        admin.setFirstName(properties.getFirstName());
        admin.setLastName(properties.getLastName());
        admin.setIsActive(true);
        admin.setIsVerified(true);
        if (admin.getRoles() == null) {
            admin.setRoles(new HashSet<>());
        }
        if (admin.getPasswordHash() == null || !passwordEncoder.matches(properties.getPassword(), admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        }

        addRole(admin, "ROLE_ADMIN");
        addRole(admin, "ROLE_SUPER_ADMIN");
        userRepository.save(admin);
        log.info("Admin bootstrap ensured for username '{}'", admin.getUsername());
    }

    private void addRole(User user, String roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        boolean exists = user.getRoles().stream().anyMatch(existing -> roleName.equals(existing.getName()));
        if (!exists) {
            user.getRoles().add(role);
        }
    }
}

