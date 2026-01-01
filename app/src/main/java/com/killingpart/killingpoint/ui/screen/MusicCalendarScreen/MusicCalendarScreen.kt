package com.killingpart.killingpoint.ui.screen.MusicCalendarScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import android.net.Uri
import androidx.compose.foundation.border

/**
 * MusicCalendarScreen
 * - 음악 일기를 달력 형태로 보여주는 메인 화면
 * - 월 선택, 날짜 선택, 선택된 일기 표시 기능 포함
 */
@Composable
fun MusicCalendarScreen(
    diaries: List<Diary>,
    navController: androidx.navigation.NavController? = null,
    initialSelectedDate: String? = null
) {
    // 현재 선택된 날짜 (null이면 아무것도 선택되지 않은 상태)
    // Diary list 로그 출력
    LaunchedEffect(diaries) {
        android.util.Log.d("MusicCalendarScreen", "=== Diary List 로그 ===")
        android.util.Log.d("MusicCalendarScreen", "총 ${diaries.size}개의 다이어리")
        diaries.forEachIndexed { index, diary ->
            android.util.Log.d("MusicCalendarScreen", 
                "Diary[$index]: " +
                "id=${diary.id}, " +
                "title=${diary.musicTitle}, " +
                "artist=${diary.artist}, " +
                "createDate=${diary.createDate}, " +
                "scope=${diary.scope}"
            )
        }
        android.util.Log.d("MusicCalendarScreen", "=== Diary List 로그 끝 ===")
    }
    
    var selectedDate by remember(initialSelectedDate) {
        mutableStateOf<LocalDate?>(
            initialSelectedDate?.let { 
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
        )
    }

    // 현재 표시 중인 달 (기본값: 현재 달 또는 선택된 날짜의 달)
    var currentMonth by remember(initialSelectedDate) { 
        mutableStateOf(
            initialSelectedDate?.let { 
                runCatching { 
                    val date = LocalDate.parse(it)
                    YearMonth.from(date)
                }.getOrNull()
            } ?: YearMonth.now()
        )
    }

    // 월 선택기(MonthPicker) 표시 여부
    var showMonthPicker by remember { mutableStateOf(false) }

    // 최근 3년간의 Month 목록 (현재 ~ 36개월 전까지)
    val availableMonths = remember {
        val months = mutableListOf<YearMonth>()
        var m = YearMonth.now()
        repeat(37) {  // 현재 달 포함 37개월
            months.add(m)
            m = m.minusMonths(1)
        }
        months
    }

    // 일기 리스트를 날짜별로 그룹화
    val diariesByDate = remember(diaries) {
        diaries.groupBy {
            runCatching {
                LocalDate.parse(it.createDate.split("T")[0])
            }.getOrNull()
        }.filterKeys { it != null }.mapKeys { it.key!! }
    }

    // 선택된 날짜의 모든 일기
    val selectedDiaries = selectedDate?.let { diariesByDate[it] } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 스크롤 가능
            .padding(horizontal = 30.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // 현재 연도 표시
        Text(
            text = "${currentMonth.year}년",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        // 현재 월 표시 + 클릭 시 MonthPicker 열기
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showMonthPicker = !showMonthPicker }
        ) {
            Text(
                text = "${currentMonth.monthValue}월",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "월 선택",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 월 선택기 표시 (토글)
        // 월 선택 토글
        if (showMonthPicker) {
            Spacer(modifier = Modifier.height(16.dp))

            // 외부 전체를 clickable로 덮지 말고, 그냥 MonthPicker만 표시
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                MonthPicker(
                    availableMonths = availableMonths,
                    selectedMonth = currentMonth,
                    onMonthSelected = { newMonth ->
                        currentMonth = newMonth
                        selectedDate = null // 달 변경 시 선택된 날짜 초기화
                    },
                    onDismiss = {
                        // 그냥 닫기만 담당
                        showMonthPicker = false
                    },
                )
            }
        }


        Spacer(Modifier.height(16.dp))

        // 요일 헤더 (SUN~SAT)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            days.forEach {
                Text(
                    text = it,
                    color = when (it) {
                        "SUN" -> Color(0xFFFF3B30)
                        "SAT" -> Color(0xFF007AFF)
                        else -> Color.White
                    },
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 달력 그리드 표시
        CalendarGrid(
            yearMonth = currentMonth,
            diariesByDate = diariesByDate,
            selectedDate = selectedDate,
            onDateClick = { date ->
                // 같은 날짜를 다시 클릭하면 선택 해제
                selectedDate = if (selectedDate == date) null else date
            }
        )

        Spacer(Modifier.height(16.dp))

        // 선택된 날짜 구분선 + 날짜 표시
        if (selectedDate != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(mainGreen)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${selectedDate!!.dayOfMonth}일",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }

        // 선택된 날짜의 모든 일기 표시
        if (selectedDiaries.isNotEmpty()) {
            // 디버깅: selectedDiaries 로그
            android.util.Log.d("MusicCalendarScreen", "selectedDiaries: ${selectedDiaries.size}개")
            selectedDiaries.forEachIndexed { index, diary ->
                android.util.Log.d("MusicCalendarScreen", "  Diary[$index]: id=${diary.id}, title=${diary.musicTitle}")
            }
            
            selectedDiaries.forEachIndexed { index, diary ->
                DiaryEntryCard(
                    diary = diary, 
                    navController = navController,
                    selectedDate = selectedDate?.toString(),
                    diaryId = diary.id
                )
            }
        } else if (selectedDate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "이 날짜에 등록된 킬링파트가 없습니다.",
                color = Color(0xFFA4A4A6),
                fontFamily = PaperlogyFontFamily,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * MonthPicker
 * - 년/월 선택 휠
 * - 닫을 때만 onMonthSelected 호출 (중간 변경은 즉시 반영 X)
 */
@Composable
fun MonthPicker(
    availableMonths: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    onDismissHandlerReady: ((() -> Unit) -> Unit)? = null, // 사용 안 해도 시그니처는 유지
    onSelectionChanged: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 년도와 월을 분리
    val years = availableMonths.map { it.year }.distinct().sorted()
    val months = (1..12).toList()

    var selectedYear by remember { mutableStateOf(selectedMonth.year) }
    var selectedMonthValue by remember { mutableStateOf(selectedMonth.monthValue) }
    var showUnsupportedMonthModal by remember { mutableStateOf(false) }
    
    // 지원되는 가장 오래된 월 (availableMonths의 마지막 항목)
    val oldestAvailableMonth = availableMonths.lastOrNull() ?: YearMonth.now()

    // onDismissHandlerReady 안 쓰더라도, 기존 구조 깨기 싫으면 빈 핸들러 넘겨줘도 됨
    LaunchedEffect(Unit) {
        onDismissHandlerReady?.invoke {
            // 필요하면 여기에 "외부에서 닫을 때" 로직 넣을 수 있음
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 년도 휠
                WheelPicker(
                    items = years.map { "${it}년" },
                    selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                    onSelected = { index ->
                        selectedYear = years[index]
                        onSelectionChanged?.invoke(selectedYear, selectedMonthValue)
                    },
                    modifier = Modifier.weight(1f)
                )

                // 월 휠
                WheelPicker(
                    items = months.map { "${it}월" },
                    selectedIndex = selectedMonthValue - 1,
                    onSelected = { index ->
                        selectedMonthValue = months[index]
                        onSelectionChanged?.invoke(selectedYear, selectedMonthValue)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 하단에 "취소 / 확인" 버튼 영역 추가
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "취소",
                    color = Color(0xFFAAAAAA),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            // 선택 반영 없이 그냥 닫기
                            onDismiss()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "확인",
                    color = mainGreen,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .pointerInput(Unit) {} // 스크롤 소비 방지
                        .clickable {
                            // 현재 선택된 년/월로 확정
                            val newMonth = YearMonth.of(selectedYear, selectedMonthValue)
                            Log.d("MonthPicker", "확인 클릭됨: $newMonth") // 로그 찍기
                            if (availableMonths.contains(newMonth)) {
                                onMonthSelected(newMonth)
                                onDismiss()
                            } else {
                                // 지원하지 않는 월 선택 시 모달 표시
                                showUnsupportedMonthModal = true
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 중앙 선택 영역 표시선 (기존 그대로 유지)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 16.dp, vertical = 32.dp) // 위/아래 살짝 여유
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGreen)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGreen)
                )
            }
        }
        
        // 지원하지 않는 월 선택 시 모달
        if (showUnsupportedMonthModal) {
            UnsupportedMonthModal(
                oldestMonth = oldestAvailableMonth,
                onDismiss = { showUnsupportedMonthModal = false }
            )
        }
    }
}

/**
 * 지원하지 않는 월 선택 시 표시되는 모달
 */
@Composable
private fun UnsupportedMonthModal(
    oldestMonth: YearMonth,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .border(width = 1.dp, color = mainGreen, shape = RoundedCornerShape(12.dp))
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${oldestMonth.year}년 ${oldestMonth.monthValue}월까지의\n킬링파트만 지원됩니다.",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(width =1.dp, color = mainGreen, shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "확인",
                        color = Color.Black,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * WheelPicker
 * - 년도/월 리스트를 휠 형태로 스크롤 선택
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 40.dp
    val visibleItems = 3
    val centerOffset = visibleItems / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))
    val density = LocalDensity.current
    var wasScrolling by remember { mutableStateOf(false) }

    // 초기 선택된 인덱스로 스크롤 (중앙에 오도록)
    LaunchedEffect(selectedIndex) {
        val targetIndex = selectedIndex.coerceAtLeast(0)
        if (listState.firstVisibleItemIndex != targetIndex) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    // 스크롤 끝난 후 중앙 항목 계산
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            wasScrolling = true
        } else if (wasScrolling) {
            wasScrolling = false
            // 중앙 항목 인덱스를 실제 offset 포함해서 계산
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val scrollOffset = listState.firstVisibleItemScrollOffset.toFloat()
            val offsetRatio = scrollOffset / itemHeightPx

            // 중앙 항목은 현재 보이는 첫 번째 항목 + offset 보정
            // contentPadding 때문에 실제로는 firstVisibleItemIndex가 중앙에 위치
            val centerIndex = (listState.firstVisibleItemIndex + offsetRatio)
                .toInt()
                .coerceIn(0, items.size - 1)

            onSelected(centerIndex)
            // 중앙 맞추기 위해 살짝 보정 스크롤
            listState.animateScrollToItem(centerIndex.coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItems)
            .clipToBounds()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = itemHeight * centerOffset,
                bottom = itemHeight * centerOffset
            )
        ) {
            itemsIndexed(items) { index, item ->
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val scrollOffset = listState.firstVisibleItemScrollOffset
                val itemHeightPx = with(density) { itemHeight.toPx() }
                val offsetRatio = scrollOffset / itemHeightPx
                // contentPadding 때문에 firstVisibleItemIndex가 실제로 중앙에 위치
                val centerIndex = (firstVisibleIndex + offsetRatio).toInt().coerceIn(0, items.size - 1)
                val isSelected = index == centerIndex
                val alpha = when (kotlin.math.abs(index - centerIndex)) {
                    0 -> 1f
                    1 -> 0.6f
                    else -> 0.3f
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) Color.White else Color(0xFF6A6B6C),
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = if (isSelected) 18.sp else 16.sp,
                        modifier = Modifier.alpha(alpha)
                    )
                }
            }
        }
    }
}


@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    diariesByDate: Map<LocalDate, List<Diary>>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek
    val daysInMonth = yearMonth.lengthOfMonth()
    
    // 첫 주의 시작 날짜 계산 (일요일부터 시작)
    val startOffset = when (firstDayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var dayCounter = 1
        var weekCounter = 0
        
        while (dayCounter <= daysInMonth || weekCounter == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    if (weekCounter == 0 && dayOfWeek < startOffset) {
                        // 이전 달의 날짜 (빈 칸)
                        Box(modifier = Modifier.size(40.dp))
                    } else if (dayCounter <= daysInMonth) {
                        val date = yearMonth.atDay(dayCounter)
                        val hasDiary = diariesByDate.containsKey(date)
                        val isSelected = selectedDate == date
                        val isToday = date == LocalDate.now()
                        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
                        val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY
                        
                        CalendarDayCell(
                            day = dayCounter,
                            hasDiary = hasDiary,
                            isSelected = isSelected,
                            isToday = isToday,
                            isSunday = isSunday,
                            isSaturday = isSaturday,
                            isCurrentMonth = true,
                            onClick = { onDateClick(date) }
                        )
                        dayCounter++
                    } else {
                        // 다음 달의 날짜 (빈 칸)
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }
            weekCounter++
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    hasDiary: Boolean,
    isSelected: Boolean,
    isToday: Boolean = false,
    isSunday: Boolean,
    isSaturday: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                if (isSelected) mainGreen // 선택된 날짜는 노란 배경
                else Color.Transparent
            )
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = mainGreen,
//                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable { onClick() }
    ) {
        // 날짜는 좌상단에 표시
        Text(
            text = day.toString(),
            color = when {
                isSelected -> Color.Black // 선택된 날짜는 검은색
                isSunday -> Color(0xFFFF3B30)
                isSaturday -> Color(0xFF007AFF)
                else -> Color.White
            },
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 4.dp)
        )
        
        // 일기가 있는 경우 음표 아이콘 표시
        if (hasDiary) {
            Image(
                painter = painterResource(
                    id = if (isSelected) R.drawable.music_note_black2
                    else R.drawable.music_note_yellow2
                ),
                contentDescription = "음악",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(22.dp)
                    .offset(y = (-7).dp) //

            )
        }
    }
}

@Composable
fun DiaryEntryCard(
    diary: Diary,
    navController: androidx.navigation.NavController? = null,
    selectedDate: String? = null,
    diaryId: Long? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        // 앨범 아트 및 정보 카드
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(Color.Black, RoundedCornerShape(12.dp))
                ,verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = diary.albumImageUrl,
                contentDescription = "앨범 아트",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.example_video),
                error = painterResource(id = R.drawable.example_video)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = diary.musicTitle,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = diary.artist,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            // 코멘트 읽기 버튼
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.clickable {
                    if (diary.id == null) {
                        android.util.Log.e("DiaryEntryCard", "diary.id가 null입니다. 일기 상세 페이지로 이동할 수 없습니다.")
                        return@clickable
                    }
                    
                    val selectedDateParam = selectedDate?.let { "&selectedDate=${Uri.encode(it)}" } ?: ""
                    val diaryIdParam = "&diaryId=${diary.id}"
                    val scopeParam = "&scope=${diary.scope.name}"
                    
                    android.util.Log.d("DiaryEntryCard", "일기 상세 페이지로 이동 - diaryId: ${diary.id}")
                    
                    val totalDurationParam = diary.totalDuration?.let { "&totalDuration=$it" } ?: ""
                    
                    navController?.navigate(
                        "diary_detail" +
                                "?artist=${Uri.encode(diary.artist)}" +
                                "&musicTitle=${Uri.encode(diary.musicTitle)}" +
                                "&albumImageUrl=${Uri.encode(diary.albumImageUrl)}" +
                                "&content=${Uri.encode(diary.content)}" +
                                "&videoUrl=${Uri.encode(diary.videoUrl)}" +
                                "&duration=${Uri.encode(diary.duration)}" +
                                "&start=${Uri.encode(diary.start)}" +
                                "&end=${Uri.encode(diary.end)}" +
                                "&createDate=${Uri.encode(diary.createDate)}" +
                                scopeParam +
                                diaryIdParam +
                                selectedDateParam +
                                totalDurationParam +
                                "&fromTab=calendar"
                    )
                }
            ) {
                Text(
                    text = "코멘트읽기",
                    color = mainGreen,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Image(
                    painter = painterResource(id = R.drawable.yellow_right_arrow),
                    contentDescription = "이동",
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

    }
}
