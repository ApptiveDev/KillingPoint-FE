package com.killingpart.killingpoint.ui.screen.OnboardingScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val CtaGreen = Color(0xFFC4F236)
private val InputBg = Color(0xFF2A2B2E)
private val ErrorRed = Color(0xFFFF6B6B)

private val nameReserved = setOf("killingpart", "admin", "support")

@Composable
fun OnboardingNameScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp)
    ) {
        val topInset = maxHeight / 3.5f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(top = topInset)) {
                Text(
                    text = "사용하실 이름이 무엇인가요?",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "이름은 언제든지 바꿀 수 있습니다",
                    color = Color(0xFF9A9B9E),
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "*한글 8자 이내, 영어 16자 이내(숫자 포함)",
                    color = Color(0xFF7E7F83),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "이름 입력",
                            color = Color(0xFF6A6B6C),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 15.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = InputBg,
                        unfocusedContainerColor = InputBg,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = CtaGreen
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                error?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = ErrorRed,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "예약어(killingpart / admin / support)는 사용할 수 없습니다.",
                    color = Color(0xFF5C5D60),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Button(
                onClick = {
                    val err = validateOnboardingName(name)
                    if (err != null) {
                        error = err
                        return@Button
                    }
                    val encoded = URLEncoder.encode(name.trim(), StandardCharsets.UTF_8.toString())
                    navController.navigate("onboarding_tag?name=$encoded&continueTutorial=true")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CtaGreen,
                    contentColor = Color(0xFF17181B)
                )
            ) {
                Text(
                    text = "다음",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

internal fun validateOnboardingName(raw: String): String? {
    val name = raw.trim()
    if (name.isEmpty()) return "이름을 입력해주세요."
    if (name.lowercase() in nameReserved) return "사용할 수 없는 이름입니다."
    if (!name.matches(Regex("^[가-힣a-zA-Z0-9]+$"))) {
        return "한글, 영문, 그리고 숫자로 조합해주세요."
    }
    val hasHangul = name.any { it in '\uAC00'..'\uD7A3' }
    return if (hasHangul) {
        if (name.length > 8) "한글 기준 8자 이내로 입력해주세요." else null
    } else {
        if (name.length > 16) "영문 기준 16자 이내로 입력해주세요." else null
    }
}
