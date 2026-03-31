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
import com.killingpart.killingpoint.navigation.OnboardingProgressStore
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.regex.Pattern

private val CtaGreen = Color(0xFFC4F236)
private val InputBg = Color(0xFF2A2B2E)
private val ErrorRed = Color(0xFFFF6B6B)

private val tagReserved = setOf("admin", "support")

@Composable
fun OnboardingTagScreen(
    navController: NavController,
    displayName: String,
    continueTutorial: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }

    var tagInput by remember { mutableStateOf("") }
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
                    text = "태그 설정 중에 나가면 처음부터 다시 진행됩니다.",
                    color = Color(0xFF6A6B6C),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "개성을 나타내는 태그를 정해주세요",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "언제든지 바꿀 수 있습니다",
                    color = Color(0xFF9A9B9E),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "*영문 소문자 4이상, 30자 이내, 특수문자 일부(._)",
                    color = Color(0xFF7E7F83),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp
                )
                if (displayName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "이름: $displayName",
                        color = Color(0xFF5C5D60),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = {
                        tagInput = it.trimStart().lowercase()
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "태그입력",
                            color = Color(0xFF6A6B6C),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 15.sp
                        )
                    },
                    prefix = { Text("@", color = Color.White, fontSize = 15.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "예약어(admin / support)는 사용할 수 없습니다.",
                    color = Color(0xFF5C5D60),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Button(
                onClick = {
                    val err = validateOnboardingTag(tagInput)
                    if (err != null) {
                        error = err
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        try {
                            val tag = tagInput.trim().removePrefix("@")
                            repo.updateTag(tag)
                                .onSuccess {
                                    if (continueTutorial) {
                                        OnboardingProgressStore.markTutorialInProgress(context)
                                        navController.navigate("onboarding_kp_intro") {
                                            popUpTo("onboarding_name") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("main") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                }
                                .onFailure { e ->
                                    error = parseTagApiErrorMessage(e.message)
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

/**
 * [AuthRepository.updateTag] 실패 시 본문이 "태그 업데이트 실패 (400): {json}" 형태이므로
 * 서버 [message] / fieldErrors.tag 등을 꺼내 온보딩에 표시한다. (프로필 설정과 동일 계열)
 */
private fun parseTagApiErrorMessage(raw: String?): String {
    if (raw.isNullOrBlank()) return "태그 설정에 실패했습니다."
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
                        if (o.has("tag")) append(o.getString("tag"))
                    }
                }.ifBlank { raw }
            }
            else -> raw
        }
    } catch (_: Exception) {
        raw
    }
}

internal fun validateOnboardingTag(raw: String): String? {
    val tag = raw.trim().removePrefix("@")
    if (tag.isEmpty()) return "태그를 입력해주세요."
    if (tag in tagReserved) return "사용할 수 없는 태그입니다."
    if (tag.length < 4 || tag.length > 30) {
        return "30자 이내의 영문과 숫자, 특수문자(._)로 조합해주세요."
    }
    val pattern = Pattern.compile("^[a-z0-9_.]+$")
    if (!pattern.matcher(tag).matches()) {
        return "30자 이내의 영문과 숫자, 특수문자(._)로 조합해주세요."
    }
    return null
}
