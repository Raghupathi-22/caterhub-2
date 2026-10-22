package com.daily.cetaring.shared.repository;

import com.daily.cetaring.features.auth.service.MobileNumberNormalizer;
import com.daily.cetaring.shared.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Admin customer listing. A customer is a non-deleted account holding ROLE_CUSTOMER and none of
     * ROLE_WORKER / ROLE_ADMIN / ROLE_SUPER_ADMIN (same rule as customer account deletion).
     * Role checks use id subqueries so a user is returned at most once regardless of role count.
     * When {@code filtered} is false the search terms are ignored.
     */
    String ADMIN_CUSTOMER_FILTER = """
        from User u
        where u.deletedAt is null
          and u.id in (select cu.id from User cu join cu.roles cr where cr.name = 'ROLE_CUSTOMER')
          and u.id not in (
              select xu.id from User xu join xu.roles xr
              where xr.name in ('ROLE_WORKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'))
          and (:filtered = false
               or lower(concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, ''))) like :namePattern
               or (:phoneFiltered = true and u.phoneNumber like :phonePattern))
        """;

    @Query(
        value = "select u " + ADMIN_CUSTOMER_FILTER,
        countQuery = "select count(u) " + ADMIN_CUSTOMER_FILTER
    )
    Page<User> findAdminCustomers(
        @Param("filtered") boolean filtered,
        @Param("namePattern") String namePattern,
        @Param("phoneFiltered") boolean phoneFiltered,
        @Param("phonePattern") String phonePattern,
        Pageable pageable
    );

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
        Optional<User> direct = findByUsernameAndDeletedAtIsNull(username);
        if (direct.isPresent()) {
            return direct;
        }
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
