package com.daily.cetaring.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.auth.OtpEntryRules

/**
 * Six-box OTP entry backed by ONE text field whose value is [code]. Boxes are purely visual, so
 * typing, paste, backspace, SMS auto-fill and keyboard OTP suggestions all flow through the same
 * state. Tapping a box selects that digit so it can be overwritten.
 *
 * The field is never disabled; [readOnly] is only used while a verification request is in flight.
 */
@Composable
fun PremiumOtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    readOnly: Boolean = false,
    autoFilled: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onDone: () -> Unit = {}
) {
    val length = OtpEntryRules.LENGTH
    var fieldValue by remember { mutableStateOf(TextFieldValue(code, TextRange(code.length))) }
    // External changes (auto-fill, reset, sanitising) win; local selection is kept otherwise.
    val value = if (fieldValue.text == code) fieldValue else TextFieldValue(code, TextRange(code.length))
    var focused by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val activeIndex = when {
        !focused -> -1
        value.selection.length == 1 -> value.selection.start
        else -> value.selection.start.coerceAtMost(length - 1)
    }

    BasicTextField(
        value = value,
        onValueChange = { next ->
            fieldValue = next
            onCodeChange(next.text)
        },
        readOnly = readOnly,
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .semantics {
                contentType = ContentType.SmsOtpCode
                contentDescription = "One-time password, $length digits"
                stateDescription = if (code.isEmpty()) "Empty" else "${code.length} of $length digits entered"
            },
        decorationBox = { innerTextField ->
            Box {
                // The real input stays in the tree for IME, paste and accessibility, but is invisible.
                Box(Modifier.alpha(0f).height(1.dp)) { innerTextField() }
                Row(
                    Modifier.fillMaxWidth().clearAndSetSemantics { },
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(length) { index ->
                        OtpDigitBox(
                            digit = code.getOrNull(index),
                            active = index == activeIndex,
                            isError = isError,
                            autoFilled = autoFilled,
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 56.dp)
                                .aspectRatio(0.86f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val target = index.coerceAtMost(code.length)
                                    fieldValue = TextFieldValue(
                                        code,
                                        if (target < code.length) TextRange(target, target + 1) else TextRange(code.length)
                                    )
                                    focusRequester.requestFocus()
                                    keyboard?.show()
                                }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpDigitBox(digit: Char?, active: Boolean, isError: Boolean, autoFilled: Boolean, modifier: Modifier) {
    val filled = digit != null
    val border by animateColorAsState(
        when {
            isError -> WorkerPalette.Maroon
            active -> WorkerPalette.Emerald
            filled -> WorkerPalette.Emerald.copy(alpha = 0.45f)
            else -> WorkerPalette.FieldBorder
        },
        tween(160),
        label = "otp-border"
    )
    val background by animateColorAsState(
        when {
            isError -> WorkerPalette.Maroon.copy(alpha = 0.05f)
            filled && autoFilled -> WorkerPalette.GoldTint
            filled -> WorkerPalette.EmeraldTint
            else -> Color.White
        },
        tween(160),
        label = "otp-bg"
    )
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .background(background, shape)
            .border(if (active) 2.dp else 1.dp, border, shape),
        contentAlignment = Alignment.Center
    ) {
        if (digit != null) {
            Text(
                digit.toString(),
                color = WorkerPalette.Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        } else if (active) {
            // Static caret (no blinking) to show where the next digit goes.
            Box(Modifier.size(width = 2.dp, height = 24.dp).background(WorkerPalette.Emerald))
        }
    }
}
