package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.CustomerAccessStatus;
import com.daily.cetaring.features.auth.dto.CustomerAccessDtos.VerifyResponse;
import com.daily.cetaring.features.auth.dto.OtpPurpose;
import com.daily.cetaring.shared.dto.AuthResponse;
import com.daily.cetaring.shared.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Unified customer authentication used by every customer-facing booking/order flow.
 *
 * <ol>
 *   <li>Client sends an OTP with purpose CUSTOMER_ACCESS (works for registered and new numbers).</li>
 *   <li>{@link #verify} checks the OTP. Existing customers receive tokens; unknown numbers receive a
 *       single-use registration ticket. No account is created at this point.</li>
 *   <li>{@link #register} redeems the ticket with the customer's full name and creates the account.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAccessService {

    static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'-]{1,79}$");

    private final OtpService otpService;
    private final AuthService authService;

    /** Not transactional: the OTP verification must commit (attempt counters) independently. */
    public VerifyResponse verify(String mobileNumber, String otp) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        otpService.verifyOtp(normalizedMobile, otp, OtpPurpose.CUSTOMER_ACCESS);

        Optional<User> existing = authService.findUserByMobile(normalizedMobile);
        if (existing.isPresent()) {
            User user = existing.get();
            requireActiveCustomer(user);
            return VerifyResponse.builder()
                    .status(CustomerAccessStatus.AUTHENTICATED)
                    .auth(authService.signInVerifiedCustomer(user))
                    .mobileNumber(normalizedMobile)
                    .build();
        }

        String ticket = otpService.issueRegistrationTicket(normalizedMobile);
        log.info("Customer verification completed for new mobile {}; awaiting profile details",
                com.daily.cetaring.shared.util.LogMasking.maskPhone(normalizedMobile));
        return VerifyResponse.builder()
                .status(CustomerAccessStatus.REGISTRATION_REQUIRED)
                .registrationToken(ticket)
                .registrationExpiresInSeconds(otpService.getRegistrationTicketSeconds())
                .mobileNumber(normalizedMobile)
                .build();
    }

    /**
     * Creates the customer account for a server-verified number. Retrying with the same ticket (for example
     * after a network timeout) signs the customer in instead of creating a duplicate account.
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AuthResponse register(String mobileNumber, String registrationToken, String fullName) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        String cleanName = normalizeFullName(fullName);

        Optional<User> existing = authService.findUserByMobile(normalizedMobile);
        existing.ifPresent(this::requireActiveCustomer);

        OtpService.RegistrationTicketState state = otpService.redeemRegistrationTicket(normalizedMobile, registrationToken);

        if (existing.isPresent()) {
            return authService.signInVerifiedCustomer(existing.get());
        }
        if (state == OtpService.RegistrationTicketState.ALREADY_REDEEMED) {
            throw new IllegalArgumentException(OtpService.REGISTRATION_EXPIRED_MESSAGE);
        }
        User user = authService.createVerifiedUser(normalizedMobile, cleanName, CUSTOMER_ROLE);
        return authService.issueTokens(user);
    }

    static String normalizeFullName(String fullName) {
        String clean = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        if (clean.length() < 2 || clean.length() > 80) {
            throw new IllegalArgumentException("Full name must be between 2 and 80 characters");
        }
        if (!NAME_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("Please enter a valid full name using letters only");
        }
        return clean;
    }

    private void requireActiveCustomer(User user) {
        if (!OtpService.isCustomerEligible(user)) {
            throw new IllegalArgumentException(OtpService.NON_CUSTOMER_ACCOUNT_MESSAGE);
        }
        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getDeletedAt() != null) {
            throw new IllegalArgumentException("This account is inactive. Please contact CaterHub support.");
        }
    }
}
