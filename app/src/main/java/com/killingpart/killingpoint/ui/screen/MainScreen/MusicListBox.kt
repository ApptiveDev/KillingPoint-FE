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
import kotlin.math.roundToInt

private const val AUTO_SCROLL_AREA_DP = 80f
private const val AUTO_SCROLL_SPEED = 18f

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

    // reorderableList는 editMode 진입 시 한 번만 복사, 이후 diaries prop 변화에 영향받지 않음
    val reorderableList = remember { mutableStateListOf<Diary>() }

    // 드래그 중인지 여부 (드래그 중엔 diaries prop 변화를 reorderableList에 반영하지 않음)
    var isDragging by remember { mutableStateOf(false) }

    var draggedItemId by remember { mutableStateOf<Long?>(null) }

    // 손가락의 뷰포트 기준 Y 좌표
    var pointerYInViewport by remember { mutableFloatStateOf(0f) }

    // swap 쿨다운
    var lastSwapTimeMs by remember { mutableLongStateOf(0L) }
    val swapCooldownMs = 120L

    // 오토스크롤용: 스크롤된 거리 누적 → visibleItemsInfo 갱신 타이밍 무관하게 swap 판단
    var scrollAccumulatedPx by remember { mutableFloatStateOf(0f) }
    var measuredItemStepPx by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }

    // editMode이고 드래그 중이 아닐 때만 외부 diaries 변화를 reorderableList에 반영
    // → onOrderChange로 DB 저장 후 diaries가 업데이트돼도 드래그가 끊기지 않음
    LaunchedEffect(diaries) {
        if (isEditMode && !isDragging) {
            reorderableList.clear()
            reorderableList.addAll(diaries)
        }
    }

    // 실제 아이템 step 측정
    LaunchedEffect(listState.layoutInfo) {
        val items = listState.layoutInfo.visibleItemsInfo
        if (items.size >= 2) {
            measuredItemStepPx = (items[1].offset - items[0].offset).toFloat()
        }
    }

    val displayList = if (isEditMode) reorderableList else diaries
    val currentDiary = diaries.getOrNull(currentIndex)
    val nextDiary = if (diaries.isNotEmpty()) diaries.getOrNull((currentIndex + 1) % diaries.size) else null
    val headerLabel = if (showCurrentHeader) "재생 중 : " else "다음곡 : "
    val headerTitle = if (showCurrentHeader) currentDiary?.musicTitle else nextDiary?.musicTitle

    // ★ 유튜브 뮤직 방식: 이웃 아이템 중간선을 넘을 때만 교체
    fun trySwap(dragDirection: Float) {
        val now = System.currentTimeMillis()
        if (now - lastSwapTimeMs < swapCooldownMs) return  // 쿨다운 중엔 무시

        val currentIdx = reorderableList.indexOfFirst { it.id == draggedItemId }
        if (currentIdx < 0) return

        val visibleItems = listState.layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return

        if (dragDirection < 0) {
            val prevListIdx = currentIdx - 1
            if (prevListIdx < 0) return

            // 2번째(인덱스 1) 아이템을 위로 끌어올릴 때,
            // 손가락이 상단 오토 스크롤 영역에 들어오면 바로 맨 위(인덱스 0)로 스냅
            if (currentIdx == 1 && pointerYInViewport < autoScrollAreaPx) {
                val item = reorderableList.removeAt(currentIdx)
                reorderableList.add(0, item)
                lastSwapTimeMs = now
                // 맨 위로 스냅되는 순간, 사용자에게 바로 보이도록 스크롤
                scope.launch {
                    listState.scrollToItem(0)
                }
                return
            }

            val prevItem = visibleItems.firstOrNull { it.index == prevListIdx } ?: return
            val prevMid = prevItem.offset + prevItem.size / 2f
            if (pointerYInViewport < prevMid) {
                val item = reorderableList.removeAt(currentIdx)
                reorderableList.add(prevListIdx, item)
                lastSwapTimeMs = now
            }
        } else if (dragDirection > 0) {
            val nextListIdx = currentIdx + 1
            if (nextListIdx >= reorderableList.size) return
            val nextItem = visibleItems.firstOrNull { it.index == nextListIdx } ?: return
            val nextMid = nextItem.offset + nextItem.size / 2f
            if (pointerYInViewport > nextMid) {
                val item = reorderableList.removeAt(currentIdx)
                reorderableList.add(nextListIdx, item)
                lastSwapTimeMs = now
            }
        }
    }

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

                        val isDraggingItem = isEditMode && d.id == draggedItemId && isDragging

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
                            dragHandleModifier = Modifier.pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(

                                    onDragStart = { offset ->
                                        if (!isEditMode) {
                                            reorderableList.clear()
                                            reorderableList.addAll(diaries)
                                            isEditMode = true
                                        }

                                        isDragging = true
                                        draggedItemId = d.id
                                        lastSwapTimeMs = 0L
                                        scrollAccumulatedPx = 0f

                                        // 드래그 시작 시 손가락 뷰포트 Y 초기화
                                        val itemInfo = listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { it.key == (d.id ?: d.hashCode()) }
                                        pointerYInViewport = itemInfo?.let {
                                            it.offset + offset.y
                                        } ?: 0f
                                    },

                                    onDragCancel = {
                                        autoScrollJob?.cancel()
                                        autoScrollJob = null
                                        isDragging = false
                                        draggedItemId = null
                                        pointerYInViewport = 0f
                                    },

                                    onDragEnd = {
                                        autoScrollJob?.cancel()
                                        autoScrollJob = null

                                        // 드래그가 끝난 시점에, 드래그하던 아이템이 맨 아래라면
                                        // 리스트 맨 아래 아이템이 완전히 보이도록 한 번 더 정렬
                                        draggedItemId?.let { id ->
                                            val lastIndex = reorderableList.lastIndex
                                            val currentIdx = reorderableList.indexOfFirst { it.id == id }
                                            if (currentIdx == lastIndex && lastIndex >= 0) {
                                                scope.launch {
                                                    // 마지막 아이템을 화면에 완전히 보이도록 위로 조금 더 당겨줌
                                                    listState.animateScrollToItem(lastIndex, scrollOffset = 0)
                                                }
                                            }
                                        }

                                        isDragging = false
                                        draggedItemId = null
                                        pointerYInViewport = 0f

                                        // 드래그 끝날 때만 DB 저장
                                        val ids = reorderableList.mapNotNull { it.id }
                                        if (ids.isNotEmpty()) onOrderChange(ids)
                                    },

                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val dragDirection = dragAmount.y
                                        pointerYInViewport += dragAmount.y

                                        val viewportHeight = (listState.layoutInfo.viewportEndOffset
                                                - listState.layoutInfo.viewportStartOffset).toFloat()

                                        // 현재 드래그 중인 아이템 인덱스
                                        val dragIdx = reorderableList.indexOfFirst { it.id == draggedItemId }

                                        // 맨 위(인덱스 0) 아이템을 아래로 끌기 시작할 때,
                                        // 먼저 0↔1 스왑이 일어나서 "위에 다른 항목이 생긴 뒤"에 보이도록 한 번 강제 스왑
                                        if (dragDirection > 0 && dragIdx == 0 && reorderableList.size > 1) {
                                            val topItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }
                                            val topMid = topItem?.let { it.offset + it.size / 2f } ?: 0f
                                            // 손가락이 첫 번째 아이템의 중간선을 넘으면 바로 0↔1 교체
                                            if (pointerYInViewport > topMid) {
                                                val item = reorderableList.removeAt(0)
                                                reorderableList.add(1, item)
                                            }
                                        }

                                        // ─── AUTO SCROLL ──────────────────────────────
                                        val inTopZone = pointerYInViewport < autoScrollAreaPx
                                        val inBottomZone = pointerYInViewport > viewportHeight - autoScrollAreaPx

                                        // 현재 리스트가 실제로 위/아래로 더 스크롤될 수 있는지 체크
                                        val canScrollUp =
                                            listState.firstVisibleItemIndex > 0 ||
                                                    listState.firstVisibleItemScrollOffset > 0
                                        val totalItems = listState.layoutInfo.totalItemsCount
                                        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                        val canScrollDown = lastVisibleIndex < totalItems - 1

                                        val shouldAutoScrollTop = inTopZone && canScrollUp
                                        // 맨 위(인덱스 0) 아이템을 아래로 끌기 시작할 때는
                                        // 먼저 0↔1 스왑이 일어나서 "위에 다른 항목이 생긴 뒤"에만 오토 스크롤 시작
                                        val isTopItemDraggingDown = dragDirection > 0 && dragIdx == 0
                                        val shouldAutoScrollBottom = inBottomZone && canScrollDown && !isTopItemDraggingDown

                                        if (shouldAutoScrollTop || shouldAutoScrollBottom) {
                                            if (autoScrollJob?.isActive != true) {
                                                autoScrollJob = scope.launch {
                                                    while (isActive) {
                                                        val vH = (listState.layoutInfo.viewportEndOffset
                                                                - listState.layoutInfo.viewportStartOffset).toFloat()
                                                        val inTop = pointerYInViewport < autoScrollAreaPx
                                                        val inBottom = pointerYInViewport > vH - autoScrollAreaPx
                                                        if (!inTop && !inBottom) break

                                                        val edge = if (inTop) pointerYInViewport else vH - pointerYInViewport
                                                        val multiplier = ((autoScrollAreaPx - edge) / autoScrollAreaPx).coerceIn(0.2f, 1f)
                                                        val delta = if (inTop) -AUTO_SCROLL_SPEED * multiplier else AUTO_SCROLL_SPEED * multiplier

                                                        val scrolled = listState.scrollBy(delta)
                                                        scrollAccumulatedPx += scrolled  // 실제 스크롤된 양 누적

                                                        // 더 이상 스크롤되지 않으면 오토스크롤 종료 (이후 trySwap이 정상동작)
                                                        if (scrolled == 0f) break

                                                        // ★ 오토스크롤 reorder: visibleItemsInfo 갱신 타이밍과 무관하게
                                                        //    스크롤 누적 거리가 아이템 한 칸을 넘으면 swap
                                                        val stepPx = measuredItemStepPx.takeIf { it > 0f } ?: continue
                                                        val steps = (scrollAccumulatedPx / stepPx).toInt()
                                                        if (steps != 0) {
                                                            val from = reorderableList.indexOfFirst { it.id == draggedItemId }
                                                            if (from >= 0) {
                                                                val to = (from + steps).coerceIn(0, reorderableList.size - 1)
                                                                if (to != from) {
                                                                    val item = reorderableList.removeAt(from)
                                                                    reorderableList.add(to, item)
                                                                }
                                                                scrollAccumulatedPx -= steps * stepPx
                                                            }
                                                        }

                                                        delay(16L)
                                                    }
                                                }
                                            }
                                        } else {
                                            autoScrollJob?.cancel()
                                            autoScrollJob = null
                                        }

                                        // ─── REORDER ──────────────────────────────────
                                        if (autoScrollJob?.isActive != true) {
                                            trySwap(dragDirection)
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