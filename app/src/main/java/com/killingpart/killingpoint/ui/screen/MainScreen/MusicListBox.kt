package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.data.model.Diary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val AUTO_SCROLL_AREA_DP = 72f
private const val AUTO_SCROLL_SPEED = 20f

@Composable
fun MusicListBox(
    currentIndex: Int,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    diaries: List<Diary>,
    showCurrentHeader: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onOrderChange: (List<Long>) -> Unit = {}
) {
    val density = LocalDensity.current
    val autoScrollAreaPx = with(density) { AUTO_SCROLL_AREA_DP.dp.toPx() }

    var isEditMode by remember { mutableStateOf(false) }
    val reorderableList = remember { mutableStateListOf<Diary>() }

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    var pointerYInViewport by remember { mutableFloatStateOf(0f) }
    var measuredItemStepPx by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }

    // reorder 전담 함수 - 딱 한 곳에서만 호출
    fun tryReorder() {
        val from = draggedIndex
        val stepPx = measuredItemStepPx
        if (from < 0 || reorderableList.isEmpty() || stepPx <= 0f) return

        val steps = (dragAccumulatedY / stepPx).toInt()
        if (steps == 0) return

        val toIndex = (from + steps).coerceIn(0, reorderableList.size - 1)
        if (toIndex != from) {
            val item = reorderableList.removeAt(from)
            reorderableList.add(toIndex, item)
            draggedIndex = toIndex
        }
        // 교체 여부 관계없이 항상 차감
        dragAccumulatedY -= steps * stepPx

        // ★ 경계(맨 위/아래)에서 같은 방향으로 계속 누적되면
        //   손가락을 반대로 돌릴 때 팍 튀는 현상 방지
        //   → 경계에 닿은 방향의 누적값은 0으로 클램프
        val atTop = draggedIndex == 0
        val atBottom = draggedIndex == reorderableList.size - 1
        if (atTop && dragAccumulatedY < 0f) dragAccumulatedY = 0f
        if (atBottom && dragAccumulatedY > 0f) dragAccumulatedY = 0f
    }

    LaunchedEffect(listState.layoutInfo) {
        val items = listState.layoutInfo.visibleItemsInfo
        if (items.size >= 2) {
            measuredItemStepPx = (items[1].offset - items[0].offset).toFloat()
        } else if (items.size == 1) {
            measuredItemStepPx = items[0].size.toFloat()
        }
    }

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
                    itemsIndexed(
                        displayList,
                        key = { _, d -> d.id ?: d.hashCode() }
                    ) { _, d ->

                        val isDraggingItem =
                            isEditMode && draggedIndex >= 0 &&
                                    reorderableList.getOrNull(draggedIndex)?.id == d.id

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

                                    onDragStart = { offset ->
                                        if (!isEditMode) {
                                            reorderableList.clear()
                                            reorderableList.addAll(diaries)
                                        }
                                        draggedIndex = reorderableList.indexOfFirst { it.id == d.id }
                                        dragAccumulatedY = 0f

                                        val items = listState.layoutInfo.visibleItemsInfo
                                        if (items.size >= 2) {
                                            measuredItemStepPx = (items[1].offset - items[0].offset).toFloat()
                                        }
                                        val itemInfo = items.firstOrNull { it.key == (d.id ?: d.hashCode()) }
                                        pointerYInViewport = itemInfo?.let { it.offset + offset.y } ?: 0f
                                    },

                                    onDragCancel = {
                                        autoScrollJob?.cancel()
                                        autoScrollJob = null
                                        draggedIndex = -1
                                        dragAccumulatedY = 0f
                                        pointerYInViewport = 0f
                                    },

                                    onDragEnd = {
                                        autoScrollJob?.cancel()
                                        autoScrollJob = null
                                        draggedIndex = -1
                                        dragAccumulatedY = 0f
                                        pointerYInViewport = 0f

                                        val ids = reorderableList.mapNotNull { it.id }
                                        if (ids.isNotEmpty()) onOrderChange(ids)
                                    },

                                    onDrag = { change, dragAmount ->
                                        change.consume()

                                        dragAccumulatedY += dragAmount.y
                                        pointerYInViewport += dragAmount.y

                                        val viewportHeight =
                                            (listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset).toFloat()

                                        // ─── AUTO SCROLL ──────────────────────────────
                                        val inTopZone = pointerYInViewport < autoScrollAreaPx
                                        val inBottomZone = pointerYInViewport > viewportHeight - autoScrollAreaPx

                                        if (inTopZone || inBottomZone) {
                                            if (autoScrollJob?.isActive != true) {
                                                autoScrollJob = scope.launch {
                                                    while (isActive) {
                                                        val vH = (listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset).toFloat()
                                                        val inTop = pointerYInViewport < autoScrollAreaPx
                                                        val inBottom = pointerYInViewport > vH - autoScrollAreaPx
                                                        if (!inTop && !inBottom) break

                                                        val edge = if (inTop) pointerYInViewport else vH - pointerYInViewport
                                                        val multiplier = ((autoScrollAreaPx - edge) / autoScrollAreaPx).coerceIn(0.2f, 1f)
                                                        val delta = if (inTop) -AUTO_SCROLL_SPEED * multiplier else AUTO_SCROLL_SPEED * multiplier

                                                        val scrolled = listState.scrollBy(delta)
                                                        dragAccumulatedY += scrolled

                                                        tryReorder()
                                                        delay(16L)
                                                    }
                                                }
                                            }
                                        } else {
                                            autoScrollJob?.cancel()
                                            autoScrollJob = null
                                        }

                                        // 오토스크롤 중이 아닐 때만 onDrag에서 reorder
                                        if (autoScrollJob?.isActive != true) {
                                            tryReorder()
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