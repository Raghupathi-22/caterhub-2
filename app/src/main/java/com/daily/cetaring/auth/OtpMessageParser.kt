package com.daily.cetaring.auth

import kotlin.math.abs

object OtpMessageParser {
    private val otpNearKeywordPattern = Regex(
        pattern = "(?i)(?:otp|one\\s*time\\s*password|verification\\s*code|code)\\D{0,24}(\\d{6})(?!\\d)"
    )
    private val sixDigitPattern = Regex("(?<!\\d)(\\d{6})(?!\\d)")
    private val otpKeywordPattern = Regex("(?i)otp|one\\s*time\\s*password|verification\\s*code|caterhub")

    fun extractOtp(message: String): String? {
        if (message.isBlank()) return null

        otpNearKeywordPattern.find(message)?.groupValues?.getOrNull(1)?.let { return it }

        val matches = sixDigitPattern.findAll(message).toList()
        if (matches.isEmpty()) return null
        if (matches.size == 1) return matches.first().groupValues[1]

        val keywordPositions = otpKeywordPattern.findAll(message).map { it.range.first }.toList()
        if (keywordPositions.isEmpty()) return matches.first().groupValues[1]

        val closest = matches.minByOrNull { match ->
            keywordPositions.minOf { keywordIndex -> abs(match.range.first - keywordIndex) }
        }
        return closest?.groupValues?.getOrNull(1)
    }
}
