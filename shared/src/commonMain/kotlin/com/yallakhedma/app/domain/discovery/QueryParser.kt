package com.yallakhedma.app.domain.discovery

/** When the customer needs the service. Drives the availability hard filter. */
enum class TimeIntent { None, Now, Today, Tomorrow }

/** A customer request split into a time constraint and the words to retrieve on. */
data class ParsedQuery(
    val raw: String,
    val time: TimeIntent,
    /** Retrieval tokens — stopwords and time words removed. */
    val tokens: List<ArabicText.Token>,
    /** Concept id → matched token indices (semantic layer). */
    val concepts: Map<String, Set<Int>>,
) {
    val isEmpty: Boolean get() = tokens.isEmpty()

    /** Share of the request pointing at each category, from its concepts. */
    val categoryWeights: Map<DiscoveryCategory, Double> by lazy {
        val acc = HashMap<DiscoveryCategory, Double>()
        for (id in concepts.keys) {
            val cats = ConceptLexicon.conceptById(id)?.categories.orEmpty()
            if (cats.isEmpty()) continue
            cats.forEach { acc[it] = (acc[it] ?: 0.0) + 1.0 / cats.size }
        }
        val total = acc.values.sum()
        if (total == 0.0) emptyMap() else acc.mapValues { it.value / total }
    }
}

object QueryParser {

    // Normalized forms (see ArabicText.normalize). Urgency words mean "today".
    private val NOW = setOf(
        "هلق", "هلء", "هسا", "هسه", "هسع", "الحين", "الحينه", "دلوقتي", "دلوقت", "حالا", "فورا", "الان",
        "now", "asap", "immediately",
    )
    private val TODAY = setOf(
        "اليوم", "هاليوم", "لليوم", "النهارده", "انهارده", "النهارده", "النهاردا", "الليله", "هالليله",
        "مستعجل", "مستعجله", "ضروري", "عاجل", "طوارء", "طواري",
        "today", "tonight", "urgent", "urgently", "emergency",
    )
    private val TOMORROW = setOf("بكرا", "بكره", "بكري", "غدا", "غدوه", "باكر", "الصبح بكرا", "tomorrow")

    fun parse(raw: String): ParsedQuery {
        val all = ArabicText.tokens(raw, dropStopwords = false)
        var time = TimeIntent.None
        val kept = ArrayList<ArabicText.Token>()
        for (t in all) {
            val intent = timeOf(t.normalized)
            if (intent != null) {
                // The most specific wins: now > today > tomorrow.
                if (time == TimeIntent.None || intent.ordinal < time.ordinal) time = intent
                continue
            }
            if (t.normalized in ArabicText.STOPWORDS || t.stem in ArabicText.STOPWORDS) continue
            kept += t
        }
        return ParsedQuery(raw = raw, time = time, tokens = kept, concepts = ConceptLexicon.match(kept))
    }

    private fun timeOf(word: String): TimeIntent? {
        // Tolerate a leading conjunction/preposition: "وبكرا", "لبكرا".
        val forms = buildList {
            add(word)
            if (word.length > 3 && (word[0] == 'و' || word[0] == 'ل' || word[0] == 'ب')) add(word.drop(1))
        }
        return when {
            forms.any { it in NOW } -> TimeIntent.Now
            forms.any { it in TODAY } -> TimeIntent.Today
            forms.any { it in TOMORROW } -> TimeIntent.Tomorrow
            else -> null
        }
    }
}
