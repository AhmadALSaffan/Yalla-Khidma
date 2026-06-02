package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.yallakhedma.app.util.CardBrand

/**
 * Groups a raw digit-only card number for display:
 *   • Amex (15 digits) → 4-6-5 (e.g. "3782 822463 10005")
 *   • Everything else  → 4-4-4-4 (e.g. "4242 4242 4242 4242")
 *
 * The state keeps only digits, so the caret always sits cleanly between
 * digits and never gets stuck on a synthetic space.
 */
internal class CardNumberVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val groups = groupsFor(digits)

        // Walk the groups, but if the input is longer than the groups can hold,
        // dump the overflow into one final group so every raw digit is rendered.
        val sb = StringBuilder()
        var consumed = 0
        val rawSeparatorAfter = mutableListOf<Int>()
        for ((i, size) in groups.withIndex()) {
            if (consumed >= digits.length) break
            if (i > 0) {
                sb.append(' ')
                rawSeparatorAfter.add(consumed - 1)
            }
            val end = minOf(consumed + size, digits.length)
            sb.append(digits, consumed, end)
            consumed = end
        }
        if (consumed < digits.length) {
            sb.append(' ')
            rawSeparatorAfter.add(consumed - 1)
            sb.append(digits, consumed, digits.length)
        }

        return TransformedText(AnnotatedString(sb.toString()), Mapping(rawSeparatorAfter))
    }

    /** Returns the group sizes for the given raw digit string. */
    private fun groupsFor(digits: String): List<Int> =
        if (CardBrand.detect(digits) == CardBrand.AMEX) listOf(4, 6, 5)
        else listOf(4, 4, 4, 4, 3) // last "3" only used for 19-digit PANs

    private class Mapping(private val rawSeparatorAfter: List<Int>) : OffsetMapping {
        // Cursor at raw position p has `spacesBefore` spaces inserted in the
        // displayed string: the count of separator-anchor indices strictly less
        // than p.
        override fun originalToTransformed(offset: Int): Int {
            val spaces = rawSeparatorAfter.count { it < offset }
            return offset + spaces
        }

        // Displayed positions of the spaces themselves are at
        // `(rawSeparatorAfter[i] + 1) + i` (each prior space shifts by one).
        override fun transformedToOriginal(offset: Int): Int {
            val displayedSpaces = rawSeparatorAfter.mapIndexed { i, raw -> raw + 1 + i }
            val spacesBefore = displayedSpaces.count { it < offset }
            return offset - spacesBefore
        }
    }
}
