package com.killingpart.killingpoint.ui.screen.CommentScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.data.model.MAX_MENTION_COUNT
import com.killingpart.killingpoint.data.model.MentionCandidate
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.CommentInputMode
import com.killingpart.killingpoint.ui.viewmodel.MentionSuggestState

private val InputBackground = Color(0xFF0A0A0A)
private val SuggestBackground = Color(0xFF252525)
private val PlaceholderGray = Color(0xFFADADAD)

@Composable
fun CommentInputBar(
    value: TextFieldValue,
    mentionSpans: List<MentionSpan>,
    inputMode: CommentInputMode,
    mentionSuggest: MentionSuggestState?,
    canSubmit: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onSubmit: () -> Unit,
    onCancelMode: () -> Unit,
    onSelectMention: (MentionCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 22.dp)
    ) {
        if (mentionSuggest != null) {
            MentionSuggestPopup(state = mentionSuggest, onSelect = onSelectMention)
        } else {
            InputModeBanner(inputMode = inputMode, onCancel = onCancelMode)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 34.dp)
                    .background(InputBackground, RoundedCornerShape(4.dp))
                    .padding(horizontal = 15.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val textStyle = TextStyle(color = Color.White, fontFamily = PaperlogyFontFamily, fontSize = 14.sp)
                val transformation = remember(mentionSpans) { MentionHighlightTransformation(mentionSpans) }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(mainGreen),
                    maxLines = 5,
                    visualTransformation = transformation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    decorationBox = { inner ->
                        if (value.text.isEmpty()) {
                            Text(text = "댓글을 달아보세요!", style = textStyle.copy(color = PlaceholderGray))
                        }
                        inner()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 34.dp)
                    .background(
                        if (canSubmit) mainGreen else mainGreen.copy(alpha = 0.4f),
                        RoundedCornerShape(4.dp)
                    )
                    .clickable(enabled = canSubmit, onClick = onSubmit),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "등록", color = Color.Black, fontFamily = PaperlogyFontFamily, fontSize = 14.sp)
            }
        }
    }
}

/** "○○ 님에게 답글 다는 중..." / "댓글 수정 중..." */
@Composable
private fun InputModeBanner(inputMode: CommentInputMode, onCancel: () -> Unit) {
    val label: AnnotatedString = when (inputMode) {
        is CommentInputMode.Reply -> buildAnnotatedString {
            if (inputMode.targetName != null) {
                withStyle(SpanStyle(color = mainGreen)) { append(inputMode.targetName) }
                append(" 님에게 답글 다는 중...")
            } else {
                append("답글 다는 중...")
            }
        }
        is CommentInputMode.Edit -> AnnotatedString("댓글 수정 중...")
        CommentInputMode.Normal -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "취소",
            tint = CommentActionGray,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onCancel)
        )
    }
}

@Composable
private fun MentionSuggestPopup(state: MentionSuggestState, onSelect: (MentionCandidate) -> Unit) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(end = 44.dp) // 등록 버튼 폭만큼 비워서 입력창과 정렬
        .heightIn(min = 34.dp)
        .background(SuggestBackground, RoundedCornerShape(4.dp))
        .padding(horizontal = 15.dp)

    Column(modifier = Modifier.padding(top = 10.dp)) {
        when {
            state.isLimitReached -> SuggestMessage(
                text = "한 댓글에 최대 ${MAX_MENTION_COUNT}명까지 언급할 수 있어요.",
                modifier = rowModifier
            )
            !state.isLoading && state.candidates.isEmpty() -> SuggestMessage(
                text = "일치하는 유저가 없어요.",
                modifier = rowModifier
            )
            else -> LazyColumn(
                modifier = Modifier.heightIn(max = 34.dp * 4),
                reverseLayout = true
            ) {
                items(state.candidates, key = { it.userId }) { candidate ->
                    MentionCandidateRow(
                        candidate = candidate,
                        keyword = state.query.keyword,
                        modifier = rowModifier.clickable { onSelect(candidate) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestMessage(text: String, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Text(text = text, color = Color(0xFFF6F6F6), fontFamily = PaperlogyFontFamily, fontSize = 12.sp)
    }
}

@Composable
private fun MentionCandidateRow(candidate: MentionCandidate, keyword: String, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // 디자인: 왼쪽 @태그 (입력한 검색어와 일치하는 앞부분 강조), 오른쪽 닉네임
        val matchLength = if (keyword.isNotEmpty() && candidate.tag.startsWith(keyword, ignoreCase = true)) {
            keyword.length
        } else 0
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = mainGreen)) { append("@" + candidate.tag.take(matchLength)) }
                append(candidate.tag.drop(matchLength))
            },
            color = Color(0xFFF6F6F6),
            fontFamily = PaperlogyFontFamily,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = candidate.username,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/** 입력창에서 확정된 멘션을 형광색으로 표시 (텍스트 길이는 그대로) */
private class MentionHighlightTransformation(
    private val spans: List<MentionSpan>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            spans.forEach { span ->
                if (span.end <= text.length) addStyle(SpanStyle(color = mainGreen), span.start, span.end)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
