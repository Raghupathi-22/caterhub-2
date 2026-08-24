package com.daily.cetaring.features.auth.service;

import com.daily.cetaring.features.auth.entity.OtpEntity;
import com.daily.cetaring.features.auth.dto.OtpPurpose;
import com.daily.cetaring.features.auth.dto.OtpSendResponse;
import com.daily.cetaring.features.auth.repository.OtpRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpSender otpSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ObjectProvider<VoiceOtpProvider> voiceOtpProviderProvider;

    @Value("${otp.dev.mode:false}")
    private boolean devMode;

    @Value("${otp.dev.code:123456}")
    private String devCode;

    @Value("${otp.expiration.seconds:300}")
    private long expirationSeconds;

    @Value("${otp.resend.cooldown.seconds:60}")
    private long resendCooldownSeconds;

    @Value("${otp.rate-limit.mobile.window-seconds:3600}")
    private long mobileRateLimitWindowSeconds;

    @Value("${otp.rate-limit.mobile.max-sends:5}")
    private long mobileRateLimitMaxSends;

    @Value("${otp.rate-limit.ip.window-seconds:3600}")
    private long ipRateLimitWindowSeconds;

    @Value("${otp.rate-limit.ip.max-sends:20}")
    private long ipRateLimitMaxSends;

    @Value("${otp.voice.rate-limit.window-seconds:3600}")
    private long voiceRateLimitWindowSeconds;

    @Value("${otp.voice.rate-limit.max-calls:2}")
    private long voiceRateLimitMaxCalls;

    public OtpSendResponse generateAndSendOtp(String mobileNumber, OtpPurpose purpose, String userType, String requesterIp) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        String purposeValue = purpose.name();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cooldownCutoff = now.minusSeconds(resendCooldownSeconds);
        LocalDateTime mobileRateLimitCutoff = now.minusSeconds(mobileRateLimitWindowSeconds);
        LocalDateTime ipRateLimitCutoff = now.minusSeconds(ipRateLimitWindowSeconds);

        validateLoginTarget(normalizedMobile, purpose, userType);

        Optional<OtpEntity> lastOtpOpt = otpRepository.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(normalizedMobile, purposeValue);
        if (lastOtpOpt.isPresent()) {
            OtpEntity lastOtp = lastOtpOpt.get();
            if (lastOtp.getCreatedAt().isAfter(cooldownCutoff)) {
                throw new IllegalArgumentException("Please wait before requesting another OTP");
            }
        }

        long mobileAttempts = otpRepository.countByMobileNumberAndPurposeAndCreatedAtAfter(
                normalizedMobile,
                purposeValue,
                mobileRateLimitCutoff
        );
        if (mobileAttempts >= mobileRateLimitMaxSends) {
            throw new IllegalArgumentException("Too many OTP requests for this mobile number. Please try again later.");
        }

        if (requesterIp != null && !requesterIp.isBlank()) {
            long ipAttempts = otpRepository.countByRequesterIpAndCreatedAtAfter(requesterIp, ipRateLimitCutoff);
            if (ipAttempts >= ipRateLimitMaxSends) {
                throw new IllegalArgumentException("Too many OTP requests from this network. Please try again later.");
            }
        }

        String rawOtp;
        if (devMode) {
            rawOtp = devCode;
            log.debug("Development OTP mode active for mobile {}", normalizedMobile);
        } else {
            SecureRandom random = new SecureRandom();
            int otpNum = 100000 + random.nextInt(900000);
            rawOtp = String.valueOf(otpNum);
        }

        OtpEntity otpEntity = OtpEntity.builder()
                .mobileNumber(normalizedMobile)
                .otpHash(passwordEncoder.encode(rawOtp))
                .purpose(purposeValue)
                .requesterIp(requesterIp)
                .attempts(0)
                .maxAttempts(5)
                .isUsed(false)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .build();

        otpRepository.save(otpEntity);

        otpSender.sendOtp(normalizedMobile, rawOtp, purposeValue);
        return OtpSendResponse.builder()
                .success(true)
                .message("OTP sent successfully")
                .expiresInSeconds(expirationSeconds)
                .build();
    }

    /**
     * Generate an OTP and request a voice-call fallback delivery. If no voice provider is configured,
     * returns a controlled "VOICE_FALLBACK_UNAVAILABLE" response rather than throwing.
     */
    public OtpSendResponse generateAndSendVoiceOtp(String mobileNumber, OtpPurpose purpose, String userType, String requesterIp) {
        VoiceOtpProvider voiceProvider = voiceOtpProviderProvider.getIfAvailable();
        if (voiceProvider == null) {
            return OtpSendResponse.builder()
                    .success(false)
                    .message("VOICE_FALLBACK_UNAVAILABLE")
                    .expiresInSeconds(0)
                    .build();
        }

        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        String purposeValue = purpose.name();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime mobileRateLimitCutoff = now.minusSeconds(voiceRateLimitWindowSeconds);

        validateLoginTarget(normalizedMobile, purpose, userType);

        long voiceAttempts = otpRepository.countByMobileNumberAndPurposeAndCreatedAtAfter(
                normalizedMobile,
                purposeValue,
                mobileRateLimitCutoff
        );
        if (voiceAttempts >= voiceRateLimitMaxCalls) {
            throw new IllegalArgumentException("Too many voice OTP requests for this mobile number. Please try again later.");
        }

        String rawOtp;
        if (devMode) {
            rawOtp = devCode;
            log.debug("Development OTP voice mode active for mobile {}", normalizedMobile);
        } else {
            SecureRandom random = new SecureRandom();
            int otpNum = 100000 + random.nextInt(900000);
            rawOtp = String.valueOf(otpNum);
        }

        OtpEntity otpEntity = OtpEntity.builder()
                .mobileNumber(normalizedMobile)
                .otpHash(passwordEncoder.encode(rawOtp))
                .purpose(purposeValue)
                .requesterIp(requesterIp)
                .attempts(0)
                .maxAttempts(5)
                .isUsed(false)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .build();

        otpRepository.save(otpEntity);

        // Delegate to provider - provider must not log OTP or sensitive details
        voiceProvider.sendVoiceOtp(normalizedMobile, rawOtp, purposeValue);

        return OtpSendResponse.builder()
                .success(true)
                .message("Voice OTP requested")
                .expiresInSeconds(expirationSeconds)
                .build();
    }

    private void validateLoginTarget(String normalizedMobile, OtpPurpose purpose, String userType) {
        if (purpose != OtpPurpose.LOGIN) {
            return;
        }
        User user = findByMobileNumber(normalizedMobile)
                .orElseThrow(() -> new IllegalArgumentException(notRegisteredMessage(userType)));
        String requiredRole = requiredRoleForUserType(userType);
        if (requiredRole != null && user.getRoles().stream().noneMatch(role -> requiredRole.equals(role.getName()))) {
            throw new IllegalArgumentException(notRegisteredMessage(userType));
        }
    }

    private String requiredRoleForUserType(String userType) {
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            return "ROLE_CUSTOMER";
        }
        if ("WORKER".equalsIgnoreCase(userType)) {
            return "ROLE_WORKER";
        }
        return null;
    }

    private String notRegisteredMessage(String userType) {
        if ("WORKER".equalsIgnoreCase(userType)) {
            return "You are not registered as a CaterHub worker. Please create a worker account first.";
        }
        return "You are not registered with CaterHub. Please create an account first.";
    }

    private Optional<User> findByMobileNumber(String normalizedMobile) {
        Optional<User> directMatch = userRepository.findByPhoneNumber(normalizedMobile);
        if (directMatch.isPresent()) {
            return directMatch;
        }
        if (normalizedMobile != null && normalizedMobile.startsWith("+91") && normalizedMobile.length() == 13) {
            return userRepository.findByPhoneNumber(normalizedMobile.substring(3));
        }
        return Optional.empty();
    }

    public boolean verifyOtp(String mobileNumber, String otp, OtpPurpose purpose) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobileNumber);
        String purposeValue = purpose.name();
        LocalDateTime now = LocalDateTime.now();

        OtpEntity otpEntity = otpRepository.findTopByMobileNumberAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(normalizedMobile, purposeValue)
                .orElseThrow(() -> new IllegalArgumentException("No active OTP found or OTP expired"));

        if (otpEntity.getExpiresAt().isBefore(now)) {
            otpEntity.setUsed(true);
            otpRepository.save(otpEntity);
            throw new IllegalArgumentException("OTP has expired");
        }

        if (otpEntity.getAttempts() >= otpEntity.getMaxAttempts()) {
            throw new IllegalArgumentException("Maximum verification attempts exceeded");
        }

        otpEntity.setLastAttemptAt(now);
        if (!passwordEncoder.matches(otp, otpEntity.getOtpHash())) {
            otpEntity.setAttempts(otpEntity.getAttempts() + 1);
            otpRepository.save(otpEntity);
            throw new IllegalArgumentException("Invalid OTP");
        }

        otpEntity.setUsed(true);
        otpEntity.setVerifiedAt(now);
        otpRepository.save(otpEntity);
        return true;
    }
}
