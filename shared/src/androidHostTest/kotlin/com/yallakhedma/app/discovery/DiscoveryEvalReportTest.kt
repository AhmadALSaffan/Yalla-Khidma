package com.yallakhedma.app.discovery

import java.io.File
import kotlin.test.Test

/**
 * Runs the keyword-vs-semantic eval and writes the report to
 * shared/build/reports/discovery/eval.md (also printed to stdout).
 *
 *     ./gradlew :shared:testAndroidHostTest --tests "*DiscoveryEvalReportTest*"
 */
class DiscoveryEvalReportTest {
    @Test
    fun writeReport() {
        val report = DiscoveryEval.report()
        val out = File("build/reports/discovery/eval.md")
        out.parentFile.mkdirs()
        out.writeText(report)
        println(report)
        println("Report written to ${out.absolutePath}")
    }
}
