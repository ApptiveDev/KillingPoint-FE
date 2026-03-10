package com.killingpart.killingpoint.ui.component


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.DiaryLikeUser
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun LikesModal(
    isLoading: Boolean,
    error: String?,
    users: List<DiaryLikeUser>,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var offsetY by remember { mutableFloatStateOf(0f) }

    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = offsetY.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF1A1A1A))
                .clickable(enabled = false) {}
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY > 100f) {
                                onDismiss()
                                offsetY = 0f
                            } else {
                                offsetY = 0f
                            }
                        },
                        onDragCancel = { offsetY = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0) offsetY += dragAmount
                        }
                    )
                }
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // 드래그 핸들
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF444444))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "좋아요",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 검색바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF101010))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 13.sp
                        ),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "친구 검색",
                                    color = Color(0xFF666666),
                                    fontFamily = PaperlogyFontFamily,
                                    fontSize = 13.sp
                                )
                            }
                            inner()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = mainGreen)
                    }
                }
                error != null -> {
                    Text(
                        text = error,
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 13.sp
                    )
                }
                users.isEmpty() -> {
                    Text(
                        text = "아직 좋아요를 누른 사람이 없어요.",
                        color = Color(0xFF888888),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    val filteredUsers = if (searchQuery.isEmpty()) users
                    else users.filter {
                        it.username.contains(searchQuery, ignoreCase = true) ||
                                it.tag.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUsers.size) { index ->
                            val user = filteredUsers[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF090909))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.profileImageUrl,
                                    contentDescription = user.username,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(50))
                                        .border(2.dp, mainGreen, RoundedCornerShape(50)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.default_profile),
                                    error = painterResource(id = R.drawable.default_profile)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.username,
                                        color = Color.White,
                                        fontFamily = PaperlogyFontFamily,
                                        fontWeight = FontWeight.W500,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "@${user.tag}",
                                        color = Color(0xFF888888),
                                        fontFamily = PaperlogyFontFamily,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}