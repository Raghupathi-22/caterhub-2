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
import java.util.LinkedHashMap;
import java.util.Map;

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
        ensureDefaultRoles();
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

    /** Recreates seed roles (V1/V8 migrations) if they were removed from the database, e.g. by a manual data reset. */
    private void ensureDefaultRoles() {
        DEFAULT_ROLES.forEach((name, description) -> {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(Role.builder().name(name).description(description).build());
                log.warn("Recreated missing role '{}'", name);
            }
        });
    }

    private static final Map<String, String> DEFAULT_ROLES = new LinkedHashMap<>();

    static {
        DEFAULT_ROLES.put("ROLE_CUSTOMER", "Customer role for booking catering services");
        DEFAULT_ROLES.put("ROLE_ADMIN", "Admin role for platform management");
        DEFAULT_ROLES.put("ROLE_KITCHEN_MANAGER", "Kitchen Manager role for preparing food");
        DEFAULT_ROLES.put("ROLE_DELIVERY_MANAGER", "Delivery Manager role for managing deliveries");
        DEFAULT_ROLES.put("ROLE_STAFF", "Staff role for serving and support");
        DEFAULT_ROLES.put("ROLE_SUPER_ADMIN", "Super Admin role for system administration");
        DEFAULT_ROLES.put("ROLE_WORKER", "Worker role for marketplace job registration, assignment, and job execution");
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

