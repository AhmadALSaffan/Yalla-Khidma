package com.yallakhedma.app.domain.discovery

/** Service categories the discovery engine can infer from a request. */
enum class DiscoveryCategory { Plumbing, Electrical, Cleaning }

/**
 * One meaning ("water is leaking", "a sink") with the many ways people say it:
 * Levantine, Gulf, Egyptian, MSA and English. A term with spaces is a phrase —
 * every word must appear in the text.
 */
data class Concept(
    val id: String,
    val categories: Set<DiscoveryCategory>,
    val terms: List<String>,
)

/**
 * The semantic layer: maps dialect and English wording onto shared concepts so
 * "مي عم تنزل من تحت المجلى" and "kitchen sink is leaking" land on the same
 * listings. Hand-written for the MVP; see docs/discovery/README.md for its
 * limits and how a server-side embedding model would replace it.
 */
object ConceptLexicon {

    private val P = setOf(DiscoveryCategory.Plumbing)
    private val E = setOf(DiscoveryCategory.Electrical)
    private val C = setOf(DiscoveryCategory.Cleaning)
    private val NONE = emptySet<DiscoveryCategory>()

    val concepts: List<Concept> = listOf(
        // ── Plumbing ──────────────────────────────────────────────────────
        Concept("plumbing", P, listOf("سباكة", "سباك", "سباكين", "أدوات صحية", "تمديدات صحية", "plumber", "plumbing")),
        Concept("water", P, listOf("مي", "مية", "ميه", "مياه", "ماء", "موية", "مايه", "water")),
        Concept("leak", P, listOf(
            "تسريب", "تسرب", "تسريبات", "يسرب", "بيسرب", "بتسرب", "سربت", "نش", "بينش", "بتنش", "رشح", "يرشح", "رطوبة",
            "نقط", "بتنقط", "بينقط", "تنقيط", "يقطر", "تقطير", "مي تنزل", "مي نازلة", "مية نازلة", "ميه تطلع", "مي طالعة",
            "leak", "leaking", "leaks", "drip", "dripping", "seeping", "water coming",
        )),
        Concept("sink", P, listOf("مجلى", "مجالي", "مغسلة", "مغاسل", "حوض", "أحواض", "sink", "basin", "washbasin")),
        Concept("tap", P, listOf("حنفية", "حنفيات", "صنبور", "خلاط", "خلاطات", "محبس", "بزبوز", "tap", "faucet", "mixer", "valve")),
        Concept("pipe", P, listOf("ماسورة", "مواسير", "انبوب", "أنابيب", "قسطل", "قساطل", "بوري", "بربيش", "وصلة مي", "وصلات", "pipe", "pipes", "hose")),
        Concept("drain_block", P, listOf(
            "انسداد", "مسدود", "مسدودة", "مسكرة", "سدة", "بالوعة", "بالوعات", "مجاري", "مجرور", "صرف", "تسليك", "طافحة", "طفح",
            "blocked", "clog", "clogged", "drain", "sewage", "unclog",
        )),
        Concept("toilet", P, listOf("تواليت", "مرحاض", "سيفون", "نياجرا", "شطاف", "كرسي حمام", "كرسي افرنجي", "toilet", "flush", "cistern")),
        Concept("heater", P + E, listOf("سخان", "سخانات", "بويلر", "مي سخنة", "ميه سخنه", "مية حارة", "heater", "boiler", "water heater", "hot water")),
        Concept("pressure", P, listOf("ضغط المي", "ضغط المياه", "مضخة", "مضخات", "ماتور مي", "موتور مي", "مواتير", "دينمو", "pump", "pressure")),
        Concept("tank", P, listOf("خزان", "خزانات", "تنك", "tank", "tanks", "cistern")),
        Concept("flood", P + C, listOf("فيضان", "غرقان", "غرقانة", "غرقت", "طوفان", "flood", "flooded", "flooding", "soaked")),

        // ── Electrical ────────────────────────────────────────────────────
        Concept("electric", E, listOf("كهرباء", "كهربا", "كهربائي", "كهربجي", "كهربائية", "electric", "electrical", "electrician")),
        Concept("power_out", E, listOf(
            "انقطاع", "قاطعة", "مقطوعة", "قطعت", "طافية", "طفت", "فصلت", "بيفصل", "بتفصل", "يفصل",
            "outage", "no power", "blackout", "power cut", "tripping", "trips",
        )),
        Concept("breaker", E, listOf("قاطع", "قواطع", "طبلون", "لوحة كهرباء", "فيوز", "ديجنتير", "breaker", "breakers", "fuse", "panel")),
        Concept("socket", E, listOf("فيشة", "فيش", "بريزة", "مقبس", "plug", "socket", "outlet")),
        Concept("wiring", E, listOf("تمديد", "تمديدات", "أسلاك", "سلك", "شريط كهربا", "خط كهرباء", "تأسيس", "wiring", "wire", "wires", "cable", "line")),
        Concept("light", E, listOf("لمبة", "لمبات", "ضو", "ضوء", "إنارة", "إضاءة", "ثريا", "ثريات", "نجفة", "سبوت", "سبوتات", "light", "lights", "lamp", "bulb", "lighting", "chandelier")),
        Concept("short_circuit", E, listOf("شرار", "شرارة", "شورت", "ماس كهربائي", "ريحة حريق", "ريحة شياط", "short circuit", "spark", "sparks", "burning smell")),
        Concept("ac", E, listOf("مكيف", "مكيفات", "تكييف", "كونديشن", "سبليت", "ac", "air conditioner", "aircon")),
        Concept("camera", E, listOf("كاميرا", "كاميرات", "انتركم", "جرس", "camera", "cctv", "intercom", "doorbell")),

        // ── Cleaning ──────────────────────────────────────────────────────
        Concept("clean", C, listOf("تنظيف", "تنضيف", "نظافة", "نضافة", "ينظف", "ينضف", "تعزيل", "يعزل", "نعزل", "clean", "cleaning", "cleaner")),
        Concept("deep_clean", C, listOf("تنظيف عميق", "تنظيف شامل", "تعزيل", "يعزل", "قبل السكن", "بعد الإخلاء", "deep clean", "move out", "move in", "before moving")),
        Concept("upholstery", C, listOf("كنب", "كنبة", "كنبايات", "سجاد", "سجادة", "موكيت", "فرشات", "مفروشات", "sofa", "couch", "carpet", "rug", "upholstery", "mattress")),
        Concept("grease", C, listOf("دهون", "دهنة", "شحم", "زيت", "شفاط", "grease", "greasy", "oily")),
        Concept("windows", C, listOf("شبابيك", "شباك", "زجاج", "قزاز", "واجهات", "window", "windows", "glass")),
        Concept("dirt", C, listOf("وسخ", "وساخة", "غبرة", "غبار", "بقع", "بقعة", "dirty", "dust", "dusty", "stain", "stains")),
        Concept("mold", C, listOf("عفن", "كلس", "تكلس", "mold", "mould", "limescale")),
        Concept("renovation", C, listOf("ترميم", "تشطيب", "بعد البناء", "دهان", "renovation", "construction", "after builders")),

        // ── Places (no category: they sharpen matches without picking a trade) ─
        Concept("kitchen", NONE, listOf("مطبخ", "مطابخ", "kitchen")),
        Concept("bathroom", NONE, listOf("حمام", "حمامات", "bathroom")),
    )

    /** Category names as they appear in `categoryTag` (Arabic or English). */
    private val categoryNames: Map<DiscoveryCategory, List<String>> = mapOf(
        DiscoveryCategory.Plumbing to listOf("سباكة", "سباك", "plumbing", "plumber"),
        DiscoveryCategory.Electrical to listOf("كهرباء", "كهربا", "electrical", "electricity", "electrician"),
        DiscoveryCategory.Cleaning to listOf("تنظيف", "نظافة", "cleaning"),
    )

    /** Pre-stemmed phrases per concept: each phrase is a list of stems. */
    internal val stemmedTerms: Map<String, List<List<String>>> =
        concepts.associate { c ->
            c.id to c.terms.map { ArabicText.stems(it) }.filter { it.isNotEmpty() }.distinct()
        }

    private val stemmedCategoryNames: Map<DiscoveryCategory, Set<String>> =
        categoryNames.mapValues { (_, names) -> names.flatMap { ArabicText.stems(it, dropStopwords = false) }.toSet() }

    fun conceptById(id: String): Concept? = concepts.firstOrNull { it.id == id }

    /** Maps a listing's `categoryTag` to a [DiscoveryCategory] (null for other trades). */
    fun categoryOf(tag: String): DiscoveryCategory? {
        val stems = ArabicText.stems(tag, dropStopwords = false)
        return stemmedCategoryNames.entries.firstOrNull { (_, names) -> stems.any { it in names } }?.key
    }

    /** True if a text token stem [token] matches lexicon stem [term]. */
    internal fun stemMatches(token: String, term: String): Boolean {
        if (token == term) return true
        // Allows dialect verb prefixes ("بيسرب") and plural/suffix variants ("leaking"),
        // but only for terms long enough not to match by accident.
        return term.length >= 3 && token.length > term.length && token.length - term.length <= 3 && token.contains(term)
    }

    /**
     * Finds concepts in [tokens]. Returns concept id → indices of the tokens that
     * matched (used to quote the listing's own words in explanations).
     */
    fun match(tokens: List<ArabicText.Token>): Map<String, Set<Int>> {
        val out = LinkedHashMap<String, Set<Int>>()
        for ((id, phrases) in stemmedTerms) {
            val hit = LinkedHashSet<Int>()
            for (phrase in phrases) {
                val idx = phrase.map { term -> tokens.indexOfFirst { stemMatches(it.stem, term) } }
                if (idx.all { it >= 0 }) hit += idx
            }
            if (hit.isNotEmpty()) out[id] = hit
        }
        return out
    }
}
