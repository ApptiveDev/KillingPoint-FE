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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val CtaGreen = Color(0xFFC4F236)
private val InputBg = Color(0xFF2A2B2E)
private val ErrorRed = Color(0xFFFF6B6B)

private val nameReserved = setOf("killingpart", "admin", "support")

@Composable
fun OnboardingNameScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }

    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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
                    text = "*1~20자, 영문·한글·숫자·띄어쓰기만 가능",
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
                    val trimmed = name.trim()
                    loading = true
                    scope.launch {
                        try {
                            repo.updateUsername(trimmed)
                                .onSuccess {
                                    val encoded = URLEncoder.encode(
                                        trimmed,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    navController.navigate(
                                        "onboarding_tag?name=$encoded&continueTutorial=true"
                                    )
                                }
                                .onFailure { e ->
                                    error = parseUsernameApiErrorMessage(e.message)
                                }
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (loading) Color(0xFF6A6B6C) else CtaGreen,
                    contentColor = Color(0xFF17181B),
                    disabledContainerColor = Color(0xFF6A6B6C),
                    disabledContentColor = Color(0xFF17181B)
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF17181B),
                        strokeWidth = 2.dp
                    )
                } else {
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
}

internal fun validateOnboardingName(raw: String): String? {
    val name = raw.trim()
    if (name.isEmpty()) return "이름을 입력해주세요."
    if (name.length > 20) return "이름은 20자 이내로 입력해주세요."
    if (name.lowercase() in nameReserved) return "사용할 수 없는 이름입니다."
    if (!name.matches(Regex("^[a-zA-Z가-힣0-9 ]+$"))) {
        return "영문, 한글, 숫자, 띄어쓰기만 사용할 수 있습니다."
    }
    return null
}

private fun parseUsernameApiErrorMessage(raw: String?): String {
    if (raw.isNullOrBlank()) return "이름 변경에 실패했습니다."
    val jsonStart = raw.indexOf("{")
    if (jsonStart == -1) return raw
    return try {
        val json = JSONObject(raw.substring(jsonStart))
        when {
            json.has("message") -> json.getString("message")
            json.has("fieldErrors") -> {
                val arr = json.getJSONArray("fieldErrors")
                buildString {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        if (o.has("username")) append(o.getString("username"))
                    }
                }.ifBlank { raw }
            }
            else -> raw
        }
    } catch (_: Exception) {
        raw
    }
}
