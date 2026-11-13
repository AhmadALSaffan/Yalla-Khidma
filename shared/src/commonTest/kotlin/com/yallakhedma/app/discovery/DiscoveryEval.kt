package com.yallakhedma.app.discovery

import com.yallakhedma.app.data.demo.DemoDiscoveryCatalog
import com.yallakhedma.app.domain.discovery.DiscoveryEngine
import com.yallakhedma.app.domain.discovery.KeywordRetriever
import com.yallakhedma.app.domain.discovery.QueryParser
import com.yallakhedma.app.domain.discovery.Retriever
import com.yallakhedma.app.domain.discovery.SearchContext
import com.yallakhedma.app.domain.discovery.SemanticRetriever
import com.yallakhedma.app.domain.model.Service
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.round

/**
 * 20 labeled requests over the 30 demo listings. Labels name the listings that
 * would actually solve the request, regardless of filters; the eval then keeps
 * only those that also pass the (identical) hard filters.
 *
 * Fixed clock: Friday 2026-11-13, 10:00 in Amman (07:00 UTC).
 */
object DiscoveryEval {

    val NOW: Instant = Instant.parse("2026-11-13T07:00:00Z")
    val TODAY: LocalDate = LocalDate(2026, 11, 13)
    val listings: List<Service> by lazy { DemoDiscoveryCatalog.services(TODAY) }
    val context = SearchContext(clientLocation = DemoDiscoveryCatalog.clientLocation, now = NOW)

    data class Labeled(val id: String, val dialect: String, val query: String, val relevant: Set<String>)

    private fun ids(vararg codes: String) = codes.map { DemoDiscoveryCatalog.ID_PREFIX + it }.toSet()

    val queries = listOf(
        Labeled("q01", "Levantine", "عندي مي عم تنزل من تحت المجلى وبدي حدا اليوم", ids("p01", "p02", "p03", "p09", "p10")),
        Labeled("q02", "Levantine", "الحنفية بتنقط طول الليل", ids("p12", "p01", "p02")),
        Labeled("q03", "English", "my kitchen sink is leaking, need a plumber today", ids("p01", "p02", "p03", "p09", "p10")),
        Labeled("q04", "Levantine", "المجاري مسدودة والمي ما عم تنزل بالمغسلة", ids("p04", "p02")),
        Labeled("q05", "Levantine", "السيفون خربان وبيضل يعبي", ids("p07")),
        Labeled("q06", "Gulf", "ابغى سباك يركب سخان ماء بكرة", ids("p05")),
        Labeled("q07", "Levantine", "ضغط المي ضعيف بالطابق الثالث", ids("p08")),
        Labeled("q08", "Levantine", "الكهربا قاطعة عن نص البيت والقاطع بيفصل", ids("e01", "e02", "e04", "e08")),
        Labeled("q09", "Levantine", "الفيشة بتطلع شرار وفي ريحة حريق", ids("e06", "e01")),
        Labeled("q10", "Egyptian", "عايز كهربائي يركب نجفة النهارده", ids("e03", "e01")),
        Labeled("q11", "English", "electrician to install ceiling lights", ids("e03", "e01")),
        Labeled("q12", "English", "need a dedicated line for my AC", ids("e05", "e01")),
        Labeled("q13", "English", "wiring for a new apartment", ids("e09", "e01")),
        Labeled("q14", "Levantine", "بدي حدا يعزّل البيت قبل ما نسكن", ids("c07", "c01")),
        Labeled("q15", "MSA", "تنظيف كنب وسجاد", ids("c02", "c09")),
        Labeled("q16", "English", "the sofa has coffee stains, can someone clean it today", ids("c02", "c09")),
        Labeled("q17", "Levantine", "المطبخ كله دهون ووسخ", ids("c03", "c01")),
        Labeled("q18", "English", "deep cleaning after renovation, lots of dust", ids("c04", "c01", "c07")),
        Labeled("q19", "Levantine", "صار عندنا فيضان مي بالبيت والسجاد غرقان", ids("c08", "p02")),
        Labeled("q20", "MSA", "الحمام فيه عفن وكلس على الحنفيات", ids("c06")),
    )

    data class Row(
        val query: Labeled,
        val targets: Set<String>,
        val ranked: List<String>,
        val hit1: Double,
        val rr: Double,
        val recall5: Double,
        val ndcg5: Double,
        val filteredOutButReturned: Int,
    )

    data class Summary(val retriever: String, val rows: List<Row>) {
        private fun mean(f: (Row) -> Double) = rows.sumOf(f) / rows.size
        val hit1 get() = mean { it.hit1 }
        val mrr get() = mean { it.rr }
        val recall5 get() = mean { it.recall5 }
        val ndcg5 get() = mean { it.ndcg5 }
        val zeroResults get() = rows.count { it.ranked.isEmpty() }
        val filterLeaks get() = rows.sumOf { it.filteredOutButReturned }
    }

    /** Listings that pass the hard filters for this query's time intent. */
    fun eligible(query: String): Set<String> {
        val time = QueryParser.parse(query).time
        return listings.filter { DiscoveryEngine.applyFilters(it, time, context) is DiscoveryEngine.FilterOutcome.Passed }
            .map { it.id }.toSet()
    }

    fun run(retriever: Retriever): Summary {
        val engine = DiscoveryEngine(retriever)
        val rows = queries.map { q ->
            val ok = eligible(q.query)
            val targets = q.relevant intersect ok
            val ranked = engine.search(q.query, listings, context).results.map { it.service.id }
            val firstHit = ranked.indexOfFirst { it in targets }
            val top5 = ranked.take(5)
            val dcg = top5.withIndex().sumOf { (i, id) -> if (id in targets) 1.0 / log2(i + 2.0) else 0.0 }
            val idcg = (0 until min(5, targets.size)).sumOf { 1.0 / log2(it + 2.0) }
            Row(
                query = q,
                targets = targets,
                ranked = ranked,
                hit1 = if (ranked.firstOrNull() in targets) 1.0 else 0.0,
                rr = if (firstHit >= 0) 1.0 / (firstHit + 1) else 0.0,
                recall5 = if (targets.isEmpty()) 0.0 else top5.count { it in targets }.toDouble() / targets.size,
                ndcg5 = if (idcg == 0.0) 0.0 else dcg / idcg,
                filteredOutButReturned = ranked.count { it !in ok },
            )
        }
        return Summary(retriever.name, rows)
    }

    private fun log2(x: Double) = ln(x) / ln(2.0)

    private fun f(x: Double) = (round(x * 100) / 100).toString().let { if (it.substringAfter('.').length == 1) it + "0" else it }
    private fun short(id: String) = id.removePrefix(DemoDiscoveryCatalog.ID_PREFIX)

    /** Markdown report comparing both retrievers under identical filters. */
    fun report(): String {
        val kw = run(KeywordRetriever())
        val sem = run(SemanticRetriever())
        return buildString {
            appendLine("# Discovery eval — keyword vs semantic")
            appendLine()
            appendLine("30 demo listings · 20 labeled requests · identical hard filters · clock fixed at $NOW (Fri 10:00 Asia/Amman) · client at Jabal Amman.")
            appendLine()
            appendLine("| Retriever | Hit@1 | MRR | Recall@5 | nDCG@5 | Zero-result queries | Filtered listings returned |")
            appendLine("|---|---|---|---|---|---|---|")
            for (s in listOf(kw, sem)) {
                appendLine("| ${s.retriever} | ${f(s.hit1)} | ${f(s.mrr)} | ${f(s.recall5)} | ${f(s.ndcg5)} | ${s.zeroResults}/20 | ${s.filterLeaks} |")
            }
            appendLine()
            appendLine("## By request language")
            appendLine()
            appendLine("| Subset | Retriever | Hit@1 | MRR | Recall@5 | nDCG@5 | Zero-result |")
            appendLine("|---|---|---|---|---|---|---|")
            for ((label, pick) in listOf<Pair<String, (Labeled) -> Boolean>>(
                "Arabic (14)" to { it.dialect != "English" },
                "English (6)" to { it.dialect == "English" },
            )) {
                for (s in listOf(kw, sem)) {
                    val sub = Summary(s.retriever, s.rows.filter { pick(it.query) })
                    appendLine("| $label | ${s.retriever} | ${f(sub.hit1)} | ${f(sub.mrr)} | ${f(sub.recall5)} | ${f(sub.ndcg5)} | ${sub.zeroResults}/${sub.rows.size} |")
                }
            }
            appendLine()
            appendLine("## Per query")
            appendLine()
            appendLine("| # | Dialect | Request | Eligible relevant | Keyword top 3 | Semantic top 3 | RR kw → sem |")
            appendLine("|---|---|---|---|---|---|---|")
            kw.rows.zip(sem.rows).forEach { (a, b) ->
                val t = a.targets.sorted().joinToString(", ") { short(it) }
                val ka = a.ranked.take(3).joinToString(", ") { short(it) }.ifEmpty { "—" }
                val sa = b.ranked.take(3).joinToString(", ") { short(it) }.ifEmpty { "—" }
                appendLine("| ${a.query.id} | ${a.query.dialect} | ${a.query.query} | $t | $ka | $sa | ${f(a.rr)} → ${f(b.rr)} |")
            }
        }
    }
}
