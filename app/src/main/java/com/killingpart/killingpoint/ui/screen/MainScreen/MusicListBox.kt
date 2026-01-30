package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.materialcore.screenHeightDp
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.serialization.json.Json.Default.configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.killingpart.killingpoint.ui.theme.mainGreen


private const val REORDER_ITEM_HEIGHT_DP = 72f

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
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { REORDER_ITEM_HEIGHT_DP.dp.toPx() }

    var isEditMode by remember { mutableStateOf(false) }
    val reorderableList = remember { mutableStateListOf<Diary>() }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val currentDiary = diaries.getOrNull(currentIndex)
    val nextDiary = if (diaries.isNotEmpty()) {
        val nextIndex = (currentIndex + 1) % diaries.size
        diaries.getOrNull(nextIndex)
    } else null
    val headerLabel = if (showCurrentHeader) "재생 중 : " else "다음곡 : "
    val headerTitle = if (showCurrentHeader) currentDiary?.musicTitle else nextDiary?.musicTitle
    val displayList = if (isEditMode) reorderableList else diaries

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
                    .background(color = Color.Black, shape = RoundedCornerShape(12.dp))
            ) {
                if (displayList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "",
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            displayList,
                            key = { _, d -> d.id ?: d.hashCode() }
                        ) { index, d ->
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
                                    dragHandleModifier = Modifier.pointerInput(index, d.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggedIndex = index
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val size = reorderableList.size
                                                if (size == 0) return@detectDragGesturesAfterLongPress
                                                val from = draggedIndex
                                                if (from < 0) return@detectDragGesturesAfterLongPress
                                                val toIndex = (from + (dragOffsetY / itemHeightPx).toInt())
                                                    .coerceIn(0, size - 1)
                                                if (toIndex != from) {
                                                    val item = reorderableList[from]
                                                    reorderableList.removeAt(from)
                                                    reorderableList.add(toIndex, item)
                                                    draggedIndex = toIndex
                                                    dragOffsetY = 0f
                                                }
                                            },
                                            onDragEnd = {
                                                draggedIndex = -1
                                                dragOffsetY = 0f
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
