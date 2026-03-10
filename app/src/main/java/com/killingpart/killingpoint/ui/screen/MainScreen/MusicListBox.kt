package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val REORDER_ITEM_HEIGHT_DP = 72f
private const val AUTO_SCROLL_AREA_DP = 72f
private const val AUTO_SCROLL_SPEED = 128f

@Composable
fun MusicListBox(
    currentIndex: Int,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    diaries: List<Diary>,
    showCurrentHeader: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onOrderChange: (List<Long>) -> Unit = {},
    onDraggingChange: (Boolean) -> Unit = {}
) {

    val density = LocalDensity.current
    val itemHeightPx = with(density) { REORDER_ITEM_HEIGHT_DP.dp.toPx() }
    val autoScrollAreaPx = with(density) { AUTO_SCROLL_AREA_DP.dp.toPx() }
    val centerDeadZonePx = with(density) { 30.dp.toPx() }

    var isEditMode by remember { mutableStateOf(false) }
    val reorderableList = remember { mutableStateListOf<Diary>() }

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val displayList = if (isEditMode) reorderableList else diaries
    val currentDiary = diaries.getOrNull(currentIndex)
    val nextDiary = if (diaries.isNotEmpty()) diaries.getOrNull((currentIndex + 1) % diaries.size) else null
    val headerLabel = if (showCurrentHeader) "재생 중 : " else "다음곡 : "
    val headerTitle = if (showCurrentHeader) currentDiary?.musicTitle else nextDiary?.musicTitle

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        NextSongList(
            title = headerTitle,
            label = headerLabel,
            expanded = expanded,
            isEditMode = isEditMode,
            onToggle = {
                if (expanded) {
                    isEditMode = false
                    onToggle(false)
                } else {
                    onToggle(true)
                }
            },
            onEditClick = {
                if (!isEditMode) {
                    reorderableList.clear()
                    reorderableList.addAll(diaries)
                    isEditMode = true
                } else {
                    val ids = reorderableList.mapNotNull { it.id }
                    if (ids.isNotEmpty()) onOrderChange(ids)
                    isEditMode = false
                }
            }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
            ) {

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    itemsIndexed(displayList, key = { _, d -> d.id ?: d.hashCode() }) { _, d ->
                        val isDraggingItem =
                            isEditMode && draggedIndex >= 0 &&
                                    reorderableList.getOrNull(draggedIndex)?.id == d.id

                        val itemModifier = if (isDraggingItem) {
                            Modifier.offset { IntOffset(0, dragOffsetY.toInt()) }
                        } else {
                            Modifier
                        }

                        Box(modifier = itemModifier) {

                            MusicListOne(
                                imageUrl = d.albumImageUrl,
                                musicTitle = d.musicTitle,
                                artist = d.artist,
                                isNow = if (d.id == currentDiary?.id) Color(0xFF060606) else Color.Transparent,
                                onClick = {
                                    if (!isEditMode) {
                                        val idx = diaries.indexOfFirst { it.id == d.id }
                                        if (idx >= 0) onItemClick(idx)
                                    }
                                },
                                isPlaying = d.id == currentDiary?.id,
                                showDragHandle = isEditMode,
                                isDragging = isDraggingItem,
                                dragHandleModifier = Modifier.pointerInput(d.id) {

                                    detectDragGesturesAfterLongPress(

                                        onDragStart = {
                                            if (!isEditMode) {
                                                reorderableList.clear()
                                                reorderableList.addAll(diaries)
                                                isEditMode = true
                                            }
                                            draggedIndex = reorderableList.indexOfFirst { it.id == d.id }
                                            dragOffsetY = 0f
                                            onDraggingChange(true)
                                        },

                                        onDragCancel = {
                                            draggedIndex = -1
                                            dragOffsetY = 0f
                                            onDraggingChange(false)
                                        },

                                        onDragEnd = {
                                            draggedIndex = -1
                                            dragOffsetY = 0f

                                            // 순서 확정
                                            val ids = reorderableList.mapNotNull { it.id }
                                            if (ids.isNotEmpty()) {
                                                onOrderChange(ids)
                                            }
                                            onDraggingChange(false)
                                        },

                                        onDrag = { change, dragAmount ->

                                            change.consumePositionChange()

                                            // ----------- REORDER -----------

                                            dragOffsetY += dragAmount.y

                                            val size = reorderableList.size
                                            val from = draggedIndex
                                            if (from < 0 || size == 0) return@detectDragGesturesAfterLongPress

                                            val toIndex = (from + (dragOffsetY / itemHeightPx).toInt())
                                                .coerceIn(0, size - 1)

                                            if (toIndex != from) {
                                                val item = reorderableList[from]
                                                reorderableList.removeAt(from)
                                                reorderableList.add(toIndex, item)
                                                draggedIndex = toIndex

                                                dragOffsetY -= (toIndex - from) * itemHeightPx
                                            }

                                            // ----------- AUTO SCROLL (가장자리 근처에서만, 점프 방지) -----------
                                            val layoutInfo = listState.layoutInfo
                                            val viewportHeight =
                                                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
                                            val pointerY = change.position.y

                                            val topZone = autoScrollAreaPx * 0.6f     // 위쪽 60% 영역 중 일부만 사용
                                            val bottomZoneStart = viewportHeight - autoScrollAreaPx * 0.6f

                                            val inTopZone = pointerY < topZone
                                            val inBottomZone = pointerY > bottomZoneStart

                                            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                            val totalCount = layoutInfo.totalItemsCount

                                            val canScrollUp = listState.firstVisibleItemIndex > 0
                                            val canScrollDown = lastVisibleIndex < totalCount - 1

                                            // 아주 작은 방향 전환(손가락 떨림)은 무시
                                            val minDirectionThreshold = 3f

                                            // 위로 끄는 제스처 + 위쪽 가장자리 근처일 때만 위로 스크롤
                                            if (dragAmount.y < -minDirectionThreshold && inTopZone && canScrollUp) {
                                                val distanceFromTop = pointerY.coerceAtLeast(0f)
                                                val zoneHeight = topZone.coerceAtLeast(1f)
                                                val factor =
                                                    ((zoneHeight - distanceFromTop) / zoneHeight)
                                                        .coerceIn(0.2f, 1f)
                                                val direction = -AUTO_SCROLL_SPEED * factor

                                                scope.launch {
                                                    listState.scrollBy(direction)
                                                }
                                            }
                                            // 아래로 끄는 제스처 + 아래쪽 가장자리 근처일 때만 아래로 스크롤
                                            else if (dragAmount.y > minDirectionThreshold && inBottomZone && canScrollDown) {
                                                val distanceFromBottom = (viewportHeight - pointerY).coerceAtLeast(0f)
                                                val zoneHeight = (viewportHeight - bottomZoneStart).coerceAtLeast(1f)
                                                val factor =
                                                    ((zoneHeight - distanceFromBottom) / zoneHeight)
                                                        .coerceIn(0.2f, 1f)
                                                val direction = AUTO_SCROLL_SPEED * factor

                                                scope.launch {
                                                    listState.scrollBy(direction)
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun LazyListState.scrollBy(direction: Float) {
    animateScrollBy(direction)
}