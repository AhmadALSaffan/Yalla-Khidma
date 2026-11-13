package com.yallakhedma.app.domain.discovery

/**
 * Text normalization shared by every retriever, so keyword and semantic search
 * see exactly the same tokens.
 *
 * Arabic: strips diacritics and tatweel, unifies alef / ya / ta-marbuta / hamza
 * seats, converts Arabic-Indic digits, then applies a deliberately light
 * prefix/suffix stemmer (no root extraction — dialect words break root
 * stemmers more often than they help).
 */
object ArabicText {

    /** A token with its stemmed form and the original surface text it came from. */
    data class Token(val stem: String, val normalized: String, val surface: String)

    fun normalize(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text.lowercase()) {
            when (ch) {
                in 'ً'..'ْ', 'ٰ', 'ـ' -> Unit // tashkeel, dagger alef, tatweel
                'أ', 'إ', 'آ', 'ٱ' -> sb.append('ا')
                'ى' -> sb.append('ي')
                'ة' -> sb.append('ه')
                'ؤ' -> sb.append('و')
                'ئ' -> sb.append('ي')
                'گ' -> sb.append('ك')
                'چ' -> sb.append('ك') // Gulf "باچر" → "باكر"
                in '٠'..'٩' -> sb.append('0' + (ch - '٠'))
                in '۰'..'۹' -> sb.append('0' + (ch - '۰'))
                else -> sb.append(if (ch.isLetterOrDigit()) ch else ' ')
            }
        }
        return sb.toString()
    }

    /** Tokenizes [text], keeping the original word for each token (for grounded reasons). */
    fun tokens(text: String, dropStopwords: Boolean = true): List<Token> {
        val out = ArrayList<Token>()
        // Split the raw text on the same boundaries normalize() produces.
        for (raw in text.split(BOUNDARY)) {
            if (raw.isBlank()) continue
            val norm = normalize(raw).trim()
            if (norm.isEmpty()) continue
            for (part in norm.split(' ')) {
                if (part.length < 2) continue
                if (dropStopwords && part in STOPWORDS) continue
                val stem = stem(part)
                if (dropStopwords && stem in STOPWORDS) continue
                out += Token(stem = stem, normalized = part, surface = raw.trim(*TRIM))
            }
        }
        return out
    }

    fun stems(text: String, dropStopwords: Boolean = true): List<String> = tokens(text, dropStopwords).map { it.stem }

    fun stem(word: String): String {
        if (word.none { it in '؀'..'ۿ' }) return stemLatin(word)
        if (word in PROTECTED) return word
        var s = word
        // Prefixes: conjunction/preposition + article. Keep at least 2–3 letters.
        val prefix = PREFIXES.firstOrNull { p -> s.startsWith(p) && s.length - p.length >= (if (p.length >= 3) 3 else 2) }
        if (prefix != null) {
            s = s.drop(prefix.length)
        } else if (s.length >= 4 && s[0] == 'و') {
            s = s.drop(1)
        }
        // Suffixes: plurals and a trailing ta-marbuta / pronoun.
        val suffix = SUFFIXES.firstOrNull { x -> s.endsWith(x) && s.length - x.length >= 3 }
        if (suffix != null) s = s.dropLast(suffix.length)
        return s
    }

    private fun stemLatin(w: String): String = when {
        w.length > 5 && w.endsWith("ing") -> w.dropLast(3)
        w.length > 4 && w.endsWith("ies") -> w.dropLast(3) + "y"
        w.length > 4 && w.endsWith("es") && (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes")) -> w.dropLast(2)
        w.length > 3 && w.endsWith("s") && !w.endsWith("ss") -> w.dropLast(1)
        w.length > 4 && w.endsWith("ed") -> w.dropLast(2)
        else -> w
    }

    private val BOUNDARY = Regex("[\\s،,.;:!?؟()\\[\\]{}\"'«»“”/\\\\|\\-–—_+*&^%$#@~`<>=]+")
    private val TRIM = charArrayOf('.', ',', '،', '؟', '?', '!', '«', '»', '"', '\'')

    private val PREFIXES = listOf("وال", "بال", "فال", "كال", "لل", "ال")
    private val SUFFIXES = listOf("ات", "ين", "ون", "ها", "هم", "ه")
    private val PROTECTED = setOf("بالوعه", "بالوعات", "بالون", "الله")

    /**
     * Function words in MSA, Levantine, Gulf and Egyptian Arabic + English.
     * Time words ("اليوم", "today", …) are handled by [QueryParser], not here.
     */
    val STOPWORDS: Set<String> = setOf(
        // Arabic (normalized forms)
        "في", "من", "على", "علي", "عن", "الى", "الي", "مع", "او", "ام", "ثم", "يا", "ان", "انه", "انو", "اني",
        "هذا", "هذه", "هذي", "هاد", "هادا", "هاي", "هيدا", "هيدي", "ده", "دي", "دا", "ذا", "هذاك",
        "اللي", "الي", "يلي", "الذي", "التي", "ما", "مش", "مو", "لا", "لم", "لن", "مافي", "ماكو", "فيه", "فيها", "فينا",
        "عندي", "عندنا", "عنا", "عندو", "عنده", "لي", "الي", "انا", "احنا", "نحن", "انت", "انتو", "هو", "هي",
        "بدي", "بدنا", "ابغي", "ابغا", "ابي", "ابا", "نبي", "نبغي", "عايز", "عاوز", "عايزه", "اريد", "نريد", "حابب", "بحتاج", "احتاج", "محتاج", "نحتاج",
        "حدا", "حد", "احد", "واحد", "شخص", "حدن", "حدي",
        "عم", "عمال", "قاعد", "كتير", "كثير", "شوي", "شويه", "جدا", "كمان", "بس", "يعني", "هيك", "كذا", "كده", "زي", "مثل",
        "تحت", "فوق", "جوا", "برا", "عند", "بين", "كل", "كله", "كلها", "صار", "صاير", "بيضل", "ضل", "يضل", "طول",
        "لو", "اذا", "ازا", "ممكن", "بليز", "لو سمحت", "شي", "اشي", "حاجه", "ايش", "شو", "وش", "ايه", "ليش", "كيف",
        "يجي", "يجيني", "يصلح", "يصلحلي", "يشوف", "تشوف", "تصليح", "اصلاح",
        // English
        "i", "im", "me", "my", "we", "our", "us", "you", "your", "a", "an", "the", "is", "are", "was", "be", "been", "am",
        "to", "of", "in", "on", "at", "for", "from", "with", "and", "or", "but", "it", "its", "this", "that", "there",
        "need", "needs", "want", "someone", "somebody", "anyone", "please", "can", "could", "help", "get", "have", "has",
        "some", "any", "lot", "lots", "very", "really", "under", "over", "into", "out", "up", "down", "who", "will", "do", "does",
        "fix", "repair", "come",
    )
}
