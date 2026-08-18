package com.daily.cetaring.shared.repository;

import com.daily.cetaring.features.auth.service.MobileNumberNormalizer;
import com.daily.cetaring.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    @Query("""
        select u from User u
        where u.deletedAt is null
          and (lower(u.email) = lower(:email) or lower(u.username) = lower(:username) or u.phoneNumber = :email)
        """)
    Optional<User> findActiveByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    default Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        Optional<User> user = findByUsernameAndDeletedAtIsNull(identifier);
        if (user.isPresent()) {
            return user;
        }

        user = findByEmailAndDeletedAtIsNull(identifier);
        if (user.isPresent()) {
            return user;
        }

        user = findByPhoneNumberAndDeletedAtIsNull(identifier);
        if (user.isPresent()) {
            return user;
        }

        try {
            String normalized = MobileNumberNormalizer.normalize(identifier);
            user = findByPhoneNumberAndDeletedAtIsNull(normalized);
            if (user.isPresent()) {
                return user;
            }
            if (normalized.startsWith("+91") && normalized.length() == 13) {
                user = findByPhoneNumberAndDeletedAtIsNull(normalized.substring(3));
                if (user.isPresent()) {
                    return user;
                }
            }
        } catch (Exception ignored) {
        }

        return findActiveByEmailOrUsername(identifier, identifier);
    }

    default Optional<User> findByUsername(String username) {
        return findByIdentifier(username);
    }

    default Optional<User> findByEmail(String email) {
        return findByEmailAndDeletedAtIsNull(email);
    }

    default Optional<User> findByPhoneNumber(String phoneNumber) {
        return findByPhoneNumberAndDeletedAtIsNull(phoneNumber);
    }

    default Optional<User> findByEmailOrUsername(String email, String username) {
        return findByIdentifier(username);
    }

    default boolean existsByUsername(String username) {
        return existsByUsernameAndDeletedAtIsNull(username);
    }

    default boolean existsByEmail(String email) {
        return existsByEmailAndDeletedAtIsNull(email);
    }

    default boolean existsByPhoneNumber(String phoneNumber) {
        return existsByPhoneNumberAndDeletedAtIsNull(phoneNumber);
    }
}
