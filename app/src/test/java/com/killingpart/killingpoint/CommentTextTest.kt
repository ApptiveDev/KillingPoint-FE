package com.killingpart.killingpoint

import com.killingpart.killingpoint.data.model.CommentAuthorDisplay
import com.killingpart.killingpoint.data.model.CommentReference
import com.killingpart.killingpoint.ui.screen.CommentScreen.CommentSegment
import com.killingpart.killingpoint.ui.screen.CommentScreen.CommentText
import com.killingpart.killingpoint.ui.screen.CommentScreen.MentionSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class CommentTextTest {

    @Test
    fun references_countAllAtSigns_andGroupSameUser() {
        val text = "a@b @이진원 @이진원 이 노래 좋다"
        val spans = listOf(
            MentionSpan(4, 8, 3, "이진원"),
            MentionSpan(9, 13, 3, "이진원")
        )
        val refs = CommentText.toReferences(text, spans)
        assertEquals(1, refs.size)
        assertEquals(listOf(2, 3), refs[0].atOrders)
        assertEquals(3, refs[0].len)
    }

    @Test
    fun segments_replaceMentionWithLatestName_andLinkify() {
        val text = "@이진원 좋아요 https://killingpart.com 봐봐"
        val refs = listOf(
            CommentReference(targetId = 3, atOrders = listOf(1), len = 3, display = CommentAuthorDisplay(username = "새이름"))
        )
        val segments = CommentText.segments(text, refs)
        assertEquals(CommentSegment.Mention("@새이름", 3), segments[0])
        assertEquals(CommentSegment.Plain(" 좋아요 "), segments[1])
        assertEquals(CommentSegment.Link("https://killingpart.com", "https://killingpart.com"), segments[2])
        assertEquals(CommentSegment.Plain(" 봐봐"), segments[3])
    }

    @Test
    fun segments_skipPlainAtSignWhenCountingOrder() {
        val text = "메일@주소 @kim 안녕"
        val refs = listOf(CommentReference(targetId = 7, atOrders = listOf(2), len = 3))
        val segments = CommentText.segments(text, refs)
        assertEquals(CommentSegment.Plain("메일@주소 "), segments[0])
        assertEquals(CommentSegment.Mention("@kim", 7), segments[1])
    }

    @Test
    fun shiftSpans_movesOnInsertBefore_andDropsOnEditInside() {
        val spans = listOf(MentionSpan(0, 4, 1, "kim"))
        val moved = CommentText.shiftSpans("@kim hi", "x@kim hi", spans)
        assertEquals(listOf(MentionSpan(1, 5, 1, "kim")), moved)

        val dropped = CommentText.shiftSpans("@kim hi", "@km hi", spans)
        assertEquals(emptyList<MentionSpan>(), dropped)
    }

    @Test
    fun mentionQuery_detectsKeyword_andIgnoresConfirmedMention() {
        val query = CommentText.mentionQueryAt("안녕 @goo", 7, emptyList())
        assertEquals(3, query?.atIndex)
        assertEquals("goo", query?.keyword)

        assertNull(CommentText.mentionQueryAt("mail@goo", 8, emptyList()))
        assertNull(CommentText.mentionQueryAt("@kim", 4, listOf(MentionSpan(0, 4, 1, "kim"))))
    }

    @Test
    fun applyMention_replacesQueryAndAddsSpan() {
        val text = "hi @go"
        val query = CommentText.mentionQueryAt(text, text.length, emptyList())!!
        val (newText, spans, cursor) = CommentText.applyMention(text, query, text.length, emptyList(), 9, "goodmusic")
        assertEquals("hi @goodmusic ", newText)
        assertEquals(listOf(MentionSpan(3, 13, 9, "goodmusic")), spans)
        assertEquals(14, cursor)
    }

    @Test
    fun relativeTime_buckets() {
        val now = LocalDateTime.of(2026, 10, 6, 12, 0)
        assertEquals("방금", CommentText.relativeTime("2026-10-06T11:59:30.000000", now))
        assertEquals("5분", CommentText.relativeTime("2026-10-06T11:55:00", now))
        assertEquals("2시간", CommentText.relativeTime("2026-10-06T09:56:00.052961", now))
        assertEquals("3일", CommentText.relativeTime("2026-10-03T12:00:00", now))
        assertEquals("2026.09.01", CommentText.relativeTime("2026-09-01T12:00:00", now))
    }

    @Test
    fun previewText_cutsAt13() {
        assertEquals("안녕하세요! 좋은 킬파...",CommentText.previewText("안녕하세요! 좋은 킬파 잘 감상했습니다. 너무"))
        assertEquals("짧은 댓글", CommentText.previewText("짧은 댓글"))
    }
}
