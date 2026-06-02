package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Displays a raw 0-4 digit string as `MM/YY` (slash auto-inserted after 2 digits).
 * The underlying [androidx.compose.material3.OutlinedTextField] keeps the value as
 * pure digits, so the caret never has to jump over the synthetic '/' — typing
 * always appends naturally and the year reads left-to-right.
 */
internal class ExpiryVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(4)
        val displayed = buildString {
            for (i in digits.indices) {
                if (i == 2) append('/')
                append(digits[i])
            }
        }
        return TransformedText(AnnotatedString(displayed), Mapping)
    }

    private object Mapping : OffsetMapping {
        // raw "1234"  →  shown "12/34"
        //  raw idx 0..2 → shown 0..2
        //  raw idx 3..4 → shown 4..5 (skip the slash at shown idx 2)
        override fun originalToTransformed(offset: Int): Int =
            if (offset <= 2) offset else offset + 1

        override fun transformedToOriginal(offset: Int): Int =
            if (offset <= 2) offset else offset - 1
    }
}
