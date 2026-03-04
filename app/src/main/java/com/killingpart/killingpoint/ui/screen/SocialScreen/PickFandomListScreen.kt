package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun PickFandomListScreen(
    navController: NavController,
    userId: Long,
    tag: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Text(
                text = "@$tag",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = mainGreen,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = "친구 검색",
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFA4A4A6)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "검색",
                    tint = mainGreen
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF232427),
                unfocusedContainerColor = Color(0xFF232427),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "픽 0",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = mainGreen
            )
            Text(
                text = "팬덤 0",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                color = Color(0xFFA4A4A6)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Todo: 추후 픽/팬덤 목록이 여기 표시됨
        }
    }
}
