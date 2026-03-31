package com.killingpart.killingpoint.ui.screen.OnboardingScreen

private val articleTitleRegex = Regex("^제\\d+조\\(.+\\)$")
private val hangulItemRegex = Regex("^([가-마])\\.\\s*(.+)$")
private val digitItemRegex = Regex("^(\\d+)\\.\\s*(.+)$")

internal fun splitPolicySections(raw: String): List<PolicySection> {
    val lines = raw.trim().split("\n")
    val sections = mutableListOf<PolicySection>()
    var currentTitle: String? = null
    val currentLines = mutableListOf<String>()
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.matches(articleTitleRegex) || trimmed == "부 칙") {
            if (currentTitle != null) {
                sections.add(PolicySection(title = currentTitle!!, lines = currentLines.toList()))
                currentLines.clear()
            }
            currentTitle = trimmed
        } else if (currentTitle != null) {
            currentLines.add(line)
        }
    }
    if (currentTitle != null) {
        sections.add(PolicySection(title = currentTitle!!, lines = currentLines.toList()))
    }
    return sections
}

internal data class PolicySection(
    val title: String,
    val lines: List<String>
)

internal sealed class PolicyBodyLine {
    data class Plain(val text: String) : PolicyBodyLine()
    data class Numbered(val index: Int, val text: String) : PolicyBodyLine()
    data object Blank : PolicyBodyLine()
}

/**
 * 조항 아래에서 [가.][나.]… 또는 [1.][2.]… 형태는 조마다 1부터 다시 번호를 붙입니다.
 */
internal fun formatSectionLines(lines: List<String>): List<PolicyBodyLine> {
    val out = mutableListOf<PolicyBodyLine>()
    var num = 0
    for (line in lines) {
        if (line.isBlank()) {
            out.add(PolicyBodyLine.Blank)
            continue
        }
        val trimmed = line.trim()
        val hangul = hangulItemRegex.find(trimmed)
        if (hangul != null) {
            num++
            out.add(PolicyBodyLine.Numbered(num, hangul.groupValues[2].trim()))
            continue
        }
        val digit = digitItemRegex.find(trimmed)
        if (digit != null) {
            num++
            out.add(PolicyBodyLine.Numbered(num, digit.groupValues[2].trim()))
            continue
        }
        out.add(PolicyBodyLine.Plain(trimmed))
    }
    return out
}
