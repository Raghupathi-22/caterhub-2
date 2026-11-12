package com.daily.cetaring.auth

/** Where the current OTP value came from. */
enum class OtpSource { NONE, MANUAL, AUTO }

/** Progress of automatic SMS detection. It never controls whether the input is editable. */
enum class OtpAutoStatus { IDLE, LISTENING, DETECTED, UNAVAILABLE, TIMED_OUT, IGNORED_MANUAL }

/**
 * Single source of truth for the OTP being entered. The six boxes on screen are derived from
 * [code]; there are no per-digit states.
 *
 * Priority rule: once the customer edits the code in a session, a late SMS detection never
 * overwrites a non-empty manual value. Auto-detection only fills an empty field or replaces a
 * previous auto-filled value from the same session.
 */
data class OtpEntryState(
    val code: String = "",
    val source: OtpSource = OtpSource.NONE,
    val manuallyEdited: Boolean = false,
    val sessionId: Int = 0,
    val autoStatus: OtpAutoStatus = OtpAutoStatus.IDLE
) {
    val isComplete: Boolean get() = code.length == OtpEntryRules.LENGTH
    val isListening: Boolean get() = autoStatus == OtpAutoStatus.LISTENING

    /** Manual entry is always available, whatever the auto-detection status. */
    val inputEnabled: Boolean get() = true
}

object OtpEntryRules {
    const val LENGTH = 6

    fun sanitize(raw: String): String = raw.filter { it in '0'..'9' }.take(LENGTH)

    /** Starts a fresh OTP session (new request or resend); stale SMS results are ignored afterwards. */
    fun startSession(state: OtpEntryState, listenForSms: Boolean): OtpEntryState = OtpEntryState(
        sessionId = state.sessionId + 1,
        autoStatus = if (listenForSms) OtpAutoStatus.LISTENING else OtpAutoStatus.IDLE
    )

    /**
     * Applies typed, pasted or deleted input. A single extra keystroke beyond six digits is ignored;
     * a pasted message or code (multi-character insert) is reduced to the OTP it contains.
     */
    fun onManualInput(state: OtpEntryState, raw: String): OtpEntryState {
        val digits = raw.filter { it in '0'..'9' }
        val next = when {
            digits.length <= LENGTH -> digits
            raw.length - state.code.length <= 1 -> state.code
            else -> OtpMessageParser.extractOtp(raw) ?: digits.takeLast(LENGTH)
        }
        if (next == state.code) return state
        return state.copy(code = next, source = OtpSource.MANUAL, manuallyEdited = true)
    }

    /** Applies an OTP detected from SMS for [sessionId]; invalid formats and stale sessions are ignored. */
    fun onAutoDetected(state: OtpEntryState, otp: String?, sessionId: Int): OtpEntryState {
        if (sessionId != state.sessionId) return state
        val code = otp?.takeIf { it.length == LENGTH && it.all { c -> c in '0'..'9' } } ?: return state
        if (state.manuallyEdited && state.code.isNotEmpty()) {
            return state.copy(autoStatus = OtpAutoStatus.IGNORED_MANUAL)
        }
        return state.copy(code = code, source = OtpSource.AUTO, autoStatus = OtpAutoStatus.DETECTED)
    }

    /** Auto-detection ended without a code (timeout, unavailable, consent dismissed). Input is unaffected. */
    fun onAutoEnded(state: OtpEntryState, sessionId: Int, status: OtpAutoStatus): OtpEntryState {
        if (sessionId != state.sessionId || state.autoStatus != OtpAutoStatus.LISTENING) return state
        return state.copy(autoStatus = status)
    }

    fun canVerify(state: OtpEntryState, isVerifying: Boolean): Boolean = state.isComplete && !isVerifying

    /** "+91 ••••••5202" style masking for display; never used for requests. */
    fun maskMobile(tenDigits: String): String {
        val digits = tenDigits.filter(Char::isDigit)
        if (digits.length < 4) return "+91 $digits"
        return "+91 " + "•".repeat(digits.length - 4) + digits.takeLast(4)
    }
}
