package com.yallakhedma.app.util

import org.jetbrains.compose.resources.DrawableResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.ic_brand_amex
import yallakhidma.shared.generated.resources.ic_brand_card
import yallakhidma.shared.generated.resources.ic_brand_discover
import yallakhidma.shared.generated.resources.ic_brand_mada
import yallakhidma.shared.generated.resources.ic_brand_mastercard
import yallakhidma.shared.generated.resources.ic_brand_visa

/** Recognised card brands, with their display name and brand-mark drawable. */
enum class CardBrand(
    val key: String,
    val displayName: String,
    val drawable: DrawableResource,
) {
    VISA("visa", "Visa", Res.drawable.ic_brand_visa),
    MASTERCARD("mastercard", "Mastercard", Res.drawable.ic_brand_mastercard),
    DISCOVER("discover", "Discover", Res.drawable.ic_brand_discover),
    AMEX("amex", "American Express", Res.drawable.ic_brand_amex),
    MADA("mada", "مدى", Res.drawable.ic_brand_mada),
    OTHER("other", "بطاقة", Res.drawable.ic_brand_card);

    companion object {
        // Mada BIN prefixes (Saudi national payment scheme — these co-brand with
        // Visa/Mastercard, so detection must run BEFORE the generic Visa/MC rules.
        // Source: SAMA published Mada BIN list (illustrative subset).
        private val MADA_BINS_6 = setOf(
            "440533", "440647", "440795", "446404", "446672", "457865", "457997",
            "458456", "462220", "468540", "474491", "483010", "484783", "489317",
            "493428", "504300", "521076", "529415", "530906", "535989", "536023",
            "537767", "543357", "557606", "588845", "588848", "604906", "636120",
            "968201", "968202", "968203", "968204", "968205", "968206", "968207",
            "968208", "968209", "968210", "968211",
        )

        /** Detects the card brand from the (possibly partial) PAN by IIN prefix. */
        fun detect(pan: String): CardBrand {
            val d = pan.filter { it.isDigit() }
            if (d.isEmpty()) return OTHER

            // Mada first — its BINs overlap Visa (4xxxxx) and Mastercard (5xxxxx) ranges.
            if (d.length >= 6 && d.substring(0, 6) in MADA_BINS_6) return MADA

            // Visa: starts with 4
            if (d.startsWith("4")) return VISA

            // Amex: 34 or 37
            if (d.length >= 2 && (d.startsWith("34") || d.startsWith("37"))) return AMEX

            // Mastercard: 51–55, OR 2221–2720
            if (d.length >= 2) {
                d.substring(0, 2).toIntOrNull()?.let { p2 ->
                    if (p2 in 51..55) return MASTERCARD
                }
            }
            if (d.length >= 4) {
                d.substring(0, 4).toIntOrNull()?.let { p4 ->
                    if (p4 in 2221..2720) return MASTERCARD
                }
            }

            // Discover: 6011, 65, or 644–649
            if (d.startsWith("6011") || d.startsWith("65")) return DISCOVER
            if (d.length >= 3) {
                d.substring(0, 3).toIntOrNull()?.let { p3 ->
                    if (p3 in 644..649) return DISCOVER
                }
            }

            return OTHER
        }

        fun fromKey(key: String): CardBrand =
            entries.firstOrNull { it.key == key } ?: OTHER
    }
}
