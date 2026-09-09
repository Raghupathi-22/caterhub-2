package com.daily.cetaring.diagnostics

import android.util.Log
import androidx.annotation.Keep

@Keep
object ReleaseDiagnostics {
    private const val TAG = "CaterHubDiag"

    @Keep
    fun info(message: String) {
        val safeMessage = message.trim()
        Log.i(TAG, safeMessage)
        println("$TAG: $safeMessage")
    }

    @Keep
    fun error(message: String) {
        val safeMessage = message.trim()
        Log.e(TAG, safeMessage)
        println("$TAG: $safeMessage")
    }
}
