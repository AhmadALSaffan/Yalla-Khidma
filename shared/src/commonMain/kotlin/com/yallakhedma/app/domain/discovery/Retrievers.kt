package com.yallakhedma.app.domain.discovery

import com.yallakhedma.app.domain.model.Service
import kotlin.math.ln
import kotlin.math.sqrt

/** A listing the retriever thinks is relevant, before any hard filter. */
data class Candidate(
    val service: Service,
    val score: Double,
    /** Words quoted verbatim from the listing that support the match. */
    val matchedTerms: List<String>,
    /** True when the match rests on the listing's category rather than its words. */
    val categoryFit: Boolean,
)

/** Turns a request into scored candidates. Filters and ranking are shared, not per retriever. */
interface Retriever {
    val name: String
    fun retrieve(query: ParsedQuery, listings: List<Service>): List<Candidate>
}

/** A listing tokenized once per search: title, description and category. */
internal class IndexedListing(val service: Service) {
    val title = ArabicText.tokens(service.title)
    val description = ArabicText.tokens(service.description)
    val tag = ArabicText.tokens(service.categoryTag)
    /** Title first, so quoted evidence prefers the title's wording. */
    val all = title + tag + description
    val category: DiscoveryCategory? = ConceptLexicon.categoryOf(service.categoryTag)

    /** Concept → (field weight, listing words that expressed it). */
    val concepts: Map<String, Pair<Double, List<String>>> by lazy {
        val out = HashMap<String, Pair<Double, List<String>>>()
        fun add(tokens: List<ArabicText.Token>, weight: Double) {
            for ((id, idx) in ConceptLexicon.match(tokens)) {
                val words = idx.sorted().map { tokens[it].surface }
                val prev = out[id]
                out[id] = if (prev == null) weight to words
                else maxOf(prev.first, weight) to (prev.second + words).distinct()
            }
        }
        add(description, 0.8)
        add(tag, 1.0)
        add(title, 1.0)
        out
    }
}

/**
 * Baseline: BM25 over normalized, lightly stemmed words (title counted twice).
 * No synonyms, no dialect knowledge, no cross-language matching.
 */
class KeywordRetriever(private val k1: Double = 1.2, private val b: Double = 0.75) : Retriever {
    override val name = "keyword (BM25)"

    override fun retrieve(query: ParsedQuery, listings: List<Service>): List<Candidate> {
        if (query.isEmpty || listings.isEmpty()) return emptyList()
        val docs = listings.map { IndexedListing(it) }
        val bags = docs.map { d -> (d.title + d.title + d.tag + d.description).map { it.stem } }
        val avgLen = bags.sumOf { it.size }.toDouble() / bags.size
        val qStems = query.tokens.map { it.stem }.distinct()
        val df = qStems.associateWith { s -> bags.count { s in it } }
        val n = docs.size.toDouble()

        return docs.mapIndexedNotNull { i, d ->
            val bag = bags[i]
            var score = 0.0
            val matched = ArrayList<String>()
            for (s in qStems) {
                val tf = bag.count { it == s }
                if (tf == 0) continue
                val idf = ln(1 + (n - df.getValue(s) + 0.5) / (df.getValue(s) + 0.5))
                score += idf * tf * (k1 + 1) / (tf + k1 * (1 - b + b * bag.size / avgLen))
                d.all.firstOrNull { it.stem == s }?.let { matched += it.surface }
            }
            if (score <= 0.0) null else Candidate(d.service, score, matched.distinct(), categoryFit = false)
        }.sortedByDescending { it.score }
    }
}

/**
 * Semantic retriever: requests and listings are projected onto shared concepts
 * (dialect + English synonyms, see [ConceptLexicon]), blended with a request →
 * category prior and character-trigram similarity for spelling variants.
 *
 *     score = 0.55 · concept coverage + 0.30 · category fit + 0.15 · trigram cosine
 */
class SemanticRetriever(private val minScore: Double = 0.25) : Retriever {
    override val name = "semantic (concepts + trigrams)"

    override fun retrieve(query: ParsedQuery, listings: List<Service>): List<Candidate> {
        if (query.isEmpty || listings.isEmpty()) return emptyList()
        val docs = listings.map { IndexedListing(it) }
        val n = docs.size.toDouble()
        val qConcepts = query.concepts.keys
        val idf = qConcepts.associateWith { c -> ln(1 + n / (1 + docs.count { c in it.concepts })) }
        val idfTotal = idf.values.sum()
        val catWeights = query.categoryWeights

        val qGrams = trigrams(query.tokens.map { it.stem })
        val docGrams = docs.map { trigrams(it.all.map { t -> t.stem }) }
        val gramDf = HashMap<String, Int>().apply { docGrams.forEach { g -> g.keys.forEach { k -> this[k] = (this[k] ?: 0) + 1 } } }

        return docs.mapIndexedNotNull { i, d ->
            var coverage = 0.0
            val evidence = ArrayList<String>()
            for (c in qConcepts) {
                val hit = d.concepts[c] ?: continue
                coverage += idf.getValue(c) * hit.first
                evidence += hit.second
            }
            val conceptScore = if (idfTotal > 0) coverage / idfTotal else 0.0
            val categoryScore = d.category?.let { catWeights[it] } ?: 0.0
            val gramScore = cosine(qGrams, docGrams[i], gramDf, n)
            // Fall back to literal word overlap so unknown-but-exact words still count as evidence.
            if (evidence.isEmpty()) {
                val qStems = query.tokens.map { it.stem }.toSet()
                d.all.filter { it.stem in qStems }.forEach { evidence += it.surface }
            }

            val score = 0.55 * conceptScore + 0.30 * categoryScore + 0.15 * gramScore
            val grounded = conceptScore > 0.0 || gramScore >= 0.15
            if (score < minScore || !grounded) null
            else Candidate(
                service = d.service,
                score = score,
                matchedTerms = evidence.distinct().take(3),
                categoryFit = categoryScore > 0.0,
            )
        }.sortedByDescending { it.score }
    }

    private fun trigrams(stems: List<String>): Map<String, Int> {
        val out = HashMap<String, Int>()
        for (s in stems) {
            val padded = " $s "
            if (padded.length < 3) continue
            for (j in 0..padded.length - 3) { val g = padded.substring(j, j + 3); out[g] = (out[g] ?: 0) + 1 }
        }
        return out
    }

    private fun cosine(q: Map<String, Int>, d: Map<String, Int>, df: Map<String, Int>, n: Double): Double {
        if (q.isEmpty() || d.isEmpty()) return 0.0
        fun w(g: String, tf: Int) = tf * ln(1 + n / (1 + (df[g] ?: 0)))
        var dot = 0.0
        for ((g, tf) in q) d[g]?.let { dot += w(g, tf) * w(g, it) }
        if (dot == 0.0) return 0.0
        val qn = sqrt(q.entries.sumOf { (g, tf) -> w(g, tf).let { it * it } })
        val dn = sqrt(d.entries.sumOf { (g, tf) -> w(g, tf).let { it * it } })
        return dot / (qn * dn)
    }
}
