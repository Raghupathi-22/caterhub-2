package com.daily.cetaring.data.repository

/**
 * OTP send rejected by the backend resend cooldown / rate limit.
 * [retryAfterSeconds] comes from the server (Retry-After header or
 * data.retryAfterSeconds); null when an older backend did not report it.
 */
class OtpRateLimitedException(
    message: String,
    val retryAfterSeconds: Int?
) : IllegalArgumentException(message) {

    companion object {
        private const val MAX_COUNTDOWN_SECONDS = 3600
        private val RETRY_AFTER_JSON = Regex("\"retryAfterSeconds\"\\s*:\\s*(\\d+)")

        fun parseRetryAfterSeconds(header: String?, body: String?): Int? {
            val seconds = header?.trim()?.toLongOrNull()
                ?: body?.let { RETRY_AFTER_JSON.find(it)?.groupValues?.getOrNull(1)?.toLongOrNull() }
            return seconds?.coerceIn(1L, MAX_COUNTDOWN_SECONDS.toLong())?.toInt()
        }

        fun isCooldownMessage(message: String): Boolean =
            message.contains("Please wait before requesting another OTP", ignoreCase = true)
    }
}
