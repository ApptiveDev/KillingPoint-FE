package com.killingpart.killingpoint.ui.screen.CommentScreen

import com.killingpart.killingpoint.data.model.CommentReference
import com.killingpart.killingpoint.data.model.MentionReferenceRequest
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 입력창에서 선택한 멘션 하나. text[start] 는 '@' 이고 text.substring(start + 1, end) == username.
 * 위치/길이는 서버와 같은 UTF-16 기준이라 Kotlin String 인덱스를 그대로 쓴다.
 */
data class MentionSpan(
    val start: Int,
    val end: Int,
    val userId: Long,
    val username: String
)

/** 본문 렌더링 단위 */
sealed interface CommentSegment {
    val text: String

    data class Plain(override val text: String) : CommentSegment
    data class Mention(override val text: String, val userId: Long) : CommentSegment
    data class Link(override val text: String, val url: String) : CommentSegment
}

/** 입력 중인 멘션 검색어. atIndex 는 '@' 위치 */
data class MentionQuery(
    val atIndex: Int,
    val keyword: String
)

object CommentText {

    private val URL_REGEX = Regex("""(https?://|www\.)[^\s]+""", RegexOption.IGNORE_CASE)

    /**
     * 본문 + references 를 멘션/링크/일반 텍스트 조각으로 나눈다.
     * 멘션은 references 의 display.username 으로 바꿔 그려서 닉네임이 바뀌어도 최신 이름이 보이게 한다.
     */
    fun segments(text: String, references: List<CommentReference>): List<CommentSegment> {
        val mentionRanges = mentionRanges(text, references)
        val result = mutableListOf<CommentSegment>()
        var cursor = 0
        for (range in mentionRanges) {
            if (range.start > cursor) result += linkify(text.substring(cursor, range.start))
            val name = range.displayName ?: text.substring(range.start + 1, range.end)
            result += CommentSegment.Mention("@$name", range.userId)
            cursor = range.end
        }
        if (cursor < text.length) result += linkify(text.substring(cursor))
        return result
    }

    private data class MentionRange(val start: Int, val end: Int, val userId: Long, val displayName: String?)

    private fun mentionRanges(text: String, references: List<CommentReference>): List<MentionRange> {
        if (references.isEmpty()) return emptyList()
        val byOrder = HashMap<Int, CommentReference>()
        references.forEach { ref -> ref.atOrders.forEach { byOrder[it] = ref } }

        val ranges = mutableListOf<MentionRange>()
        var order = 0
        var i = 0
        while (i < text.length) {
            if (text[i] == '@') {
                order++
                val ref = byOrder[order]
                val end = i + 1 + (ref?.len ?: 0)
                if (ref != null && ref.len > 0 && end <= text.length) {
                    ranges += MentionRange(i, end, ref.targetId, ref.display?.username?.takeIf { it.isNotBlank() })
                    // 멘션 범위 안의 '@' 도 서버는 순서에 센다
                    order += text.substring(i + 1, end).count { it == '@' }
                    i = end
                    continue
                }
            }
            i++
        }
        return ranges
    }

    private fun linkify(text: String): List<CommentSegment> {
        val result = mutableListOf<CommentSegment>()
        var cursor = 0
        URL_REGEX.findAll(text).forEach { match ->
            if (match.range.first > cursor) result += CommentSegment.Plain(text.substring(cursor, match.range.first))
            val raw = match.value
            val url = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
            result += CommentSegment.Link(raw, url)
            cursor = match.range.last + 1
        }
        if (cursor < text.length) result += CommentSegment.Plain(text.substring(cursor))
        return result
    }

    /**
     * 텍스트가 바뀌었을 때 기존 멘션 범위를 따라 옮긴다.
     * 수정 구간과 겹치거나 더 이상 '@닉네임' 과 일치하지 않는 멘션은 해제한다.
     */
    fun shiftSpans(oldText: String, newText: String, spans: List<MentionSpan>): List<MentionSpan> {
        if (spans.isEmpty() || oldText == newText) return spans.filter { matches(newText, it) }

        var prefix = 0
        val maxPrefix = minOf(oldText.length, newText.length)
        while (prefix < maxPrefix && oldText[prefix] == newText[prefix]) prefix++

        var suffix = 0
        val maxSuffix = minOf(oldText.length, newText.length) - prefix
        while (suffix < maxSuffix &&
            oldText[oldText.length - 1 - suffix] == newText[newText.length - 1 - suffix]
        ) suffix++

        val changeStart = prefix
        val oldChangeEnd = oldText.length - suffix
        val delta = newText.length - oldText.length

        return spans.mapNotNull { span ->
            when {
                span.end <= changeStart -> span
                span.start >= oldChangeEnd -> span.copy(start = span.start + delta, end = span.end + delta)
                else -> null
            }
        }.filter { matches(newText, it) }
    }

    private fun matches(text: String, span: MentionSpan): Boolean =
        span.start >= 0 && span.end <= text.length &&
            text[span.start] == '@' &&
            text.substring(span.start + 1, span.end) == span.username

    /**
     * 커서 앞에서 입력 중인 '@검색어' 를 찾는다. 이미 확정된 멘션 안이거나 공백이 끼면 null.
     */
    fun mentionQueryAt(text: String, cursor: Int, spans: List<MentionSpan>): MentionQuery? {
        if (cursor <= 0 || cursor > text.length) return null
        var i = cursor - 1
        while (i >= 0) {
            val c = text[i]
            if (c == '@') break
            if (c.isWhitespace()) return null
            i--
        }
        if (i < 0) return null
        if (i > 0 && !text[i - 1].isWhitespace()) return null
        if (spans.any { i >= it.start && i < it.end }) return null
        return MentionQuery(atIndex = i, keyword = text.substring(i + 1, cursor))
    }

    /** '@검색어' 를 '@닉네임 ' 으로 바꾸고 멘션을 추가한다. 반환: 새 텍스트, 새 멘션 목록, 새 커서 */
    fun applyMention(
        text: String,
        query: MentionQuery,
        cursor: Int,
        spans: List<MentionSpan>,
        userId: Long,
        username: String
    ): Triple<String, List<MentionSpan>, Int> {
        val inserted = "@$username "
        val newText = text.substring(0, query.atIndex) + inserted + text.substring(cursor)
        val shifted = shiftSpans(text, newText, spans)
        val span = MentionSpan(query.atIndex, query.atIndex + 1 + username.length, userId, username)
        return Triple(newText, (shifted + span).sortedBy { it.start }, query.atIndex + inserted.length)
    }

    /** 멘션 대상 수 (같은 사람 여러 번은 1명) */
    fun mentionedUserCount(spans: List<MentionSpan>): Int = spans.map { it.userId }.distinct().size

    /**
     * 요청용 references 생성. atOrders 는 본문 전체에서 몇 번째 '@' 인지 (1부터, 일반 '@' 포함).
     * 앞뒤 공백을 trim 해서 보낼 때는 trim 후 텍스트와 그에 맞게 옮긴 spans 를 넘겨야 한다.
     */
    fun toReferences(text: String, spans: List<MentionSpan>): List<MentionReferenceRequest> {
        if (spans.isEmpty()) return emptyList()
        val valid = spans.filter { matches(text, it) }.sortedBy { it.start }
        return valid.groupBy { it.userId }.map { (userId, userSpans) ->
            MentionReferenceRequest(
                targetId = userId,
                atOrders = userSpans.map { span -> text.substring(0, span.start + 1).count { it == '@' } },
                len = userSpans.first().username.length
            )
        }
    }

    /** 서버 references 로부터 입력창 멘션 복원 (댓글 수정 시작 시) */
    fun spansFromReferences(text: String, references: List<CommentReference>): List<MentionSpan> =
        mentionRanges(text, references).map { range ->
            MentionSpan(range.start, range.end, range.userId, text.substring(range.start + 1, range.end))
        }

    /** trim 한 텍스트에 맞춰 멘션 위치를 옮긴다 */
    fun trimWithSpans(text: String, spans: List<MentionSpan>): Pair<String, List<MentionSpan>> {
        val leading = text.length - text.trimStart().length
        val trimmed = text.trim()
        val moved = spans.map { it.copy(start = it.start - leading, end = it.end - leading) }
            .filter { matches(trimmed, it) }
        return trimmed to moved
    }

    /** 미리보기 문구: 13자 + "..." */
    fun previewText(content: String, maxLength: Int = 13): String {
        val singleLine = content.replace('\n', ' ').trim()
        return if (singleLine.length > maxLength) singleLine.take(maxLength).trimEnd() + "..." else singleLine
    }

    fun countLabel(count: Int): String = if (count > 99) "99+" else count.toString()

    private val SERVER_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val FALLBACK_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    /** 상대 시간: 방금 / n분 / n시간 / n일 / yyyy.MM.dd (7일 이상) */
    fun relativeTime(createDate: String, now: LocalDateTime = LocalDateTime.now()): String {
        val created = runCatching { LocalDateTime.parse(createDate, SERVER_DATE_FORMAT) }.getOrNull()
            ?: return ""
        val diff = Duration.between(created, now)
        return when {
            diff.toMinutes() < 1 -> "방금"
            diff.toHours() < 1 -> "${diff.toMinutes()}분"
            diff.toDays() < 1 -> "${diff.toHours()}시간"
            diff.toDays() < 7 -> "${diff.toDays()}일"
            else -> created.format(FALLBACK_DATE_FORMAT)
        }
    }
}
