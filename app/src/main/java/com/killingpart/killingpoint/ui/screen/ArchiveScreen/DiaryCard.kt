package com.killingpart.killingpoint.ui.screen.ArchiveScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import androidx.compose.material3.Surface

@Composable
fun DiaryCard(
    diary: Diary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // 날짜에서 시간 부분 제거하고 포맷 변경 (예: "2025-10-29T23:52:08" -> "2025.10.29")
    val dateOnly = try {
        val datePart = diary.createDate.split("T")[0] // ISO 형식에서 날짜 부분만 추출
        datePart.replace("-", ".") // "-"를 "."으로 변경
    } catch (e: Exception) {
        // 파싱 실패 시 원본 문자열 반환 (이미 날짜만 있는 경우)
        diary.createDate
    }
    
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
    ) {
        // 상단 아이콘들 (좋아요 + 공개 범위)
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp)
//                .padding(horizontal = 3.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // 좋아요 수
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Favorite,
//                    contentDescription = "Likes",
//                    tint = Color(0xFFCCFF33),
//                    modifier = Modifier.size(23.dp)
//                )
//                Spacer(modifier = Modifier.width(3.dp))
//                Text(
//                    text = "25", // TODO: 실제 좋아요 수로 교체
//                    color = Color.White,
//                    fontSize = 15.sp,
//                    fontFamily = PaperlogyFontFamily,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            // 공개 범위 아이콘
//            Icon(
//                imageVector = when (diary.scope) {
//                    Scope.PUBLIC -> Icons.Filled.Language
//                    Scope.PRIVATE -> Icons.Filled.Lock
//                    Scope.KILLING_PART -> Icons.Filled.MusicNote
//                },
//                contentDescription = "Scope",
//                tint = Color.White,
//                modifier = Modifier.size(23.dp)
//            )
//        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // 앨범 이미지
            AsyncImage(
                model = diary.albumImageUrl,
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.example_video),
                error = painterResource(id = R.drawable.example_video)
            )
            
            // 오버레이 그라데이션 (텍스트 가독성을 위해)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    )
            )
        }
        
        // 하단 텍스트 정보 (이미지 아래)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 노래 제목
            Text(
                text = diary.musicTitle,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 아티스트명
            Text(
                text = diary.artist,
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(5.dp))
            
            // 날짜
            Text(
                text = dateOnly,
                color = Color.White,
                fontSize = 8.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiaryCardPreview() {
    Surface(
        color = Color(0xFF060606)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val mockDiary = Diary(
                artist = "Michael Jackson",
                musicTitle = "Xscape",
                albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                content = "목데이터1",
                videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                scope = Scope.PUBLIC,
                duration = "string",
                start = "string",
                end = "string",
                createDate = "1999.12.12",
                updateDate = "string"
            )
            
            DiaryCard(
                diary = mockDiary
            )
        }
    }
}
