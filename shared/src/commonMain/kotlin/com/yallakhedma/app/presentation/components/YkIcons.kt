package com.yallakhedma.app.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * App icon set — 24×24 stroke icons (Lucide geometry, 2px stroke, round caps)
 * so every screen shares one consistent icon language. Tint with Icon(tint=…).
 */
object YkIcons {
    val ArrowBack by lazy { stroke("arrow_back", autoMirror = true, "M19 12H5", "M12 19l-7-7 7-7") }
    val ChevronForward by lazy { stroke("chevron_fwd", autoMirror = true, "M9 18l6-6-6-6") }
    val Close by lazy { stroke("close", false, "M18 6L6 18", "M6 6l12 12") }
    val Check by lazy { stroke("check", false, "M20 6L9 17l-5-5") }
    val Mail by lazy {
        stroke(
            "mail", false,
            "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
            "M22 7l-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7",
        )
    }
    val Lock by lazy {
        stroke(
            "lock", false,
            "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z",
            "M7 11V7a5 5 0 0 1 10 0v4",
        )
    }
    val Eye by lazy {
        stroke(
            "eye", false,
            "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z",
            "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z",
        )
    }
    val EyeOff by lazy {
        stroke(
            "eye_off", false,
            "M9.88 9.88a3 3 0 1 0 4.24 4.24",
            "M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68",
            "M6.61 6.61A13.53 13.53 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61",
            "M2 2l20 20",
        )
    }
    val User by lazy {
        stroke(
            "user", false,
            "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2",
            "M12 3a4 4 0 1 0 0 8a4 4 0 1 0 0-8z",
        )
    }
    val Search by lazy { stroke("search", false, "M11 3a8 8 0 1 0 0 16a8 8 0 1 0 0-16z", "M21 21l-4.35-4.35") }
    val Briefcase by lazy {
        stroke(
            "briefcase", false,
            "M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16",
            "M4 6h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z",
        )
    }
    val Globe by lazy {
        stroke(
            "globe", false,
            "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20z",
            "M2 12h20",
            "M12 2a15.3 15.3 0 0 1 4 10a15.3 15.3 0 0 1-4 10a15.3 15.3 0 0 1-4-10a15.3 15.3 0 0 1 4-10z",
        )
    }
    val ShieldCheck by lazy {
        stroke(
            "shield_check", false,
            "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
            "M9 12l2 2 4-4",
        )
    }
    val MailCheck by lazy {
        stroke(
            "mail_check", false,
            "M22 13V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h8",
            "M22 7l-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7",
            "M16 19l2 2 4-4",
        )
    }
    val KeyRound by lazy {
        stroke(
            "key_round", false,
            "M2.59 18.41A2 2 0 0 0 2 19.83V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.17a2 2 0 0 0 1.42-.59l.81-.81a6.5 6.5 0 1 0-4.24-4.24z",
            "M16.5 7a.5.5 0 1 0 0 1a.5.5 0 1 0 0-1z",
        )
    }
    val MapPin by lazy {
        stroke(
            "map_pin", false,
            "M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0z",
            "M12 7a3 3 0 1 0 0 6a3 3 0 1 0 0-6z",
        )
    }
    val Wrench by lazy {
        stroke(
            "wrench", false,
            "M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z",
        )
    }
    val Message by lazy { stroke("message", false, "M7.9 20A9 9 0 1 0 4 16.1L2 22z") }

    val Bell by lazy {
        stroke(
            "bell", false,
            "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9",
            "M10.3 21a1.94 1.94 0 0 0 3.4 0",
        )
    }
    val Phone by lazy {
        stroke(
            "phone", false,
            "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.13.96.36 1.9.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.91.34 1.85.57 2.81.7A2 2 0 0 1 22 16.92z",
        )
    }
    val Clock by lazy { stroke("clock", false, "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20z", "M12 6v6l4 2") }
    val Grid by lazy {
        stroke(
            "grid", false,
            "M4 3h6a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M14 3h6a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1h-6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M14 13h6a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1h-6a1 1 0 0 1-1-1v-6a1 1 0 0 1 1-1z",
            "M4 13h6a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-6a1 1 0 0 1 1-1z",
        )
    }
    val CreditCard by lazy {
        stroke(
            "credit_card", false,
            "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z",
            "M2 10h20",
        )
    }
    val HelpCircle by lazy {
        stroke(
            "help", false,
            "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20z",
            "M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3",
            "M12 17h.01",
        )
    }
    val Info by lazy { stroke("info", false, "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20z", "M12 16v-4", "M12 8h.01") }
    val LogOut by lazy {
        stroke(
            "log_out", autoMirror = true,
            "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
            "M16 17l5-5-5-5",
            "M21 12H9",
        )
    }
    val Edit by lazy {
        stroke(
            "edit", false,
            "M12 20h9",
            "M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z",
        )
    }
    val Camera by lazy {
        stroke(
            "camera", false,
            "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3z",
            "M12 10a3 3 0 1 0 0 6a3 3 0 1 0 0-6z",
        )
    }
    val Sparkles by lazy {
        stroke(
            "sparkles", false,
            "M9.94 15.5A2 2 0 0 0 8.5 14.06l-6.13-1.58a.5.5 0 0 1 0-.96L8.5 9.94A2 2 0 0 0 9.94 8.5l1.58-6.13a.5.5 0 0 1 .96 0L14.06 8.5A2 2 0 0 0 15.5 9.94l6.13 1.58a.5.5 0 0 1 0 .96L15.5 14.06a2 2 0 0 0-1.44 1.44l-1.58 6.13a.5.5 0 0 1-.96 0z",
        )
    }
    val CalendarCheck by lazy {
        stroke(
            "calendar_check", false,
            "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
            "M16 2v4", "M8 2v4", "M3 10h18", "M9 16l2 2 4-4",
        )
    }

    val Bolt by lazy { stroke("bolt", false, "M13 2L3 14h9l-1 8 10-12h-9l1-8z") }
    val Book by lazy {
        stroke(
            "book", false,
            "M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z",
            "M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z",
        )
    }
    val IdCard by lazy {
        stroke(
            "id_card", false,
            "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z",
            "M16 10h2", "M16 14h2",
            "M6.17 15a3 3 0 0 1 5.66 0",
            "M9 9a2 2 0 1 0 0 4a2 2 0 1 0 0-4z",
        )
    }
    val Hourglass by lazy {
        stroke(
            "hourglass", false,
            "M5 22h14", "M5 2h14",
            "M17 22v-4.17a2 2 0 0 0-.59-1.42L12 12l-4.41 4.41A2 2 0 0 0 7 17.83V22",
            "M7 2v4.17a2 2 0 0 0 .59 1.42L12 12l4.41-4.41A2 2 0 0 0 17 6.17V2",
        )
    }
    val Plus by lazy { stroke("plus", false, "M12 5v14", "M5 12h14") }
    val Ticket by lazy {
        stroke(
            "ticket", false,
            "M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2z",
            "M13 5v2", "M13 17v2", "M13 11v2",
        )
    }
    val Headphones by lazy {
        stroke(
            "headphones", false,
            "M3 14h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a9 9 0 0 1 18 0v7a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3",
        )
    }
    val Clipboard by lazy {
        stroke(
            "clipboard", false,
            "M9 2h6a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1z",
            "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2",
            "M12 11h4", "M12 16h4", "M8 11h.01", "M8 16h.01",
        )
    }
    val Wallet by lazy {
        stroke(
            "wallet", false,
            "M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1",
            "M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4",
        )
    }

    // ── Filled set (bottom navigation) ───────────────────────────────────────
    val HomeFilled by lazy {
        fill(
            "home_filled",
            "M10.6 2.6a2.2 2.2 0 0 1 2.8 0l7.2 6a2.2 2.2 0 0 1 .8 1.7v9.2a2.2 2.2 0 0 1-2.2 2.2h-3.4a1 1 0 0 1-1-1v-4.6a2.8 2.8 0 0 0-5.6 0v4.6a1 1 0 0 1-1 1H4.8a2.2 2.2 0 0 1-2.2-2.2v-9.2a2.2 2.2 0 0 1 .8-1.7z",
        )
    }
    val SearchFilled by lazy {
        fill(
            "search_filled",
            "M10.5 2a8.5 8.5 0 0 1 6.75 13.67l4.04 4.04a1.12 1.12 0 0 1-1.58 1.58l-4.04-4.04A8.5 8.5 0 1 1 10.5 2zm0 2.4a6.1 6.1 0 1 0 0 12.2a6.1 6.1 0 0 0 0-12.2z",
        )
    }
    val CalendarFilled by lazy {
        fill(
            "calendar_filled",
            "M7.5 2a1 1 0 0 1 1 1v1h7V3a1 1 0 1 1 2 0v1h1a3 3 0 0 1 3 3v1.5H2.5V7a3 3 0 0 1 3-3h1V3a1 1 0 0 1 1-1zM2.5 10.5h19V19a3 3 0 0 1-3 3h-13a3 3 0 0 1-3-3zm5 3.5a1.2 1.2 0 1 0 0 2.4a1.2 1.2 0 0 0 0-2.4zm4.5 0a1.2 1.2 0 1 0 0 2.4a1.2 1.2 0 0 0 0-2.4z",
        )
    }
    val UserFilled by lazy {
        fill(
            "user_filled",
            "M12 2a5 5 0 1 1 0 10a5 5 0 0 1 0-10zM12 13.5c4.6 0 8.3 2.8 8.3 6.3a2.2 2.2 0 0 1-2.2 2.2H5.9a2.2 2.2 0 0 1-2.2-2.2c0-3.5 3.7-6.3 8.3-6.3z",
        )
    }
    val GridFilled by lazy {
        fill(
            "grid_filled",
            "M5 3h4a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM15 3h4a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM5 13h4a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2zM15 13h4a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2z",
        )
    }
    val BriefcaseFilled by lazy {
        fill(
            "briefcase_filled",
            "M9.5 2h5A2.5 2.5 0 0 1 17 4.5V6h2.5A2.5 2.5 0 0 1 22 8.5v3.2H2V8.5A2.5 2.5 0 0 1 4.5 6H7V4.5A2.5 2.5 0 0 1 9.5 2zm0 2a.5.5 0 0 0-.5.5V6h6V4.5a.5.5 0 0 0-.5-.5zM2 13.7h8.5v1.1a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-1.1H22v4.8A2.5 2.5 0 0 1 19.5 21h-15A2.5 2.5 0 0 1 2 18.5z",
        )
    }

    /** Filled star (rating). */
    val StarFilled by lazy {
        fill("star", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z")
    }

    private fun stroke(name: String, autoMirror: Boolean, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    private fun fill(name: String, d: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // Even-odd so inner sub-paths (search ring, calendar dots) cut holes.
            addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd)
        }.build()
}
