package com.killingpart.killingpoint.ui.screen.OnboardingScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.screen.HomeScreen.BackgroundVideo
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.activityScopedLoginViewModel
import kotlinx.coroutines.launch

@Composable
fun PolicyAgreementScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = activityScopedLoginViewModel()
    val loginState by loginViewModel.state.collectAsState()
    val isNewUser = (loginState as? LoginUiState.AutoLoginSuccess)?.isNew == true
    val scope = rememberCoroutineScope()
    var serviceAgreed by remember { mutableStateOf(false) }
    var privacyAgreed by remember { mutableStateOf(false) }
    var selectedPolicyType by remember { mutableStateOf<OnboardingPolicyType?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundVideo(
            videoFileName = "login_video.mp4",
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(id = R.drawable.killing_part_logo_white2),
            contentDescription = "KillingPart Logo",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 200.dp, start = 20.dp)
                .size(width = 320.dp, height = 110.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            AgreementRow(
                checked = serviceAgreed,
                label = "서비스 이용약관",
                onCheckedChange = { serviceAgreed = it },
                onViewFullClick = { selectedPolicyType = OnboardingPolicyType.SERVICE_TERMS }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AgreementRow(
                checked = privacyAgreed,
                label = "개인정보 처리방침",
                onCheckedChange = { privacyAgreed = it },
                onViewFullClick = { selectedPolicyType = OnboardingPolicyType.PRIVACY }
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 기획 메모 기준: 비활성화 처리 없이 기본 CTA 노출
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(Color(0xFFC4F236), RoundedCornerShape(100))
                    .clickable {
                        scope.launch {
                            serviceAgreed = true
                            privacyAgreed = true
                            val repo = AuthRepository(context)
                            if (!repo.agreeRequiredPolicies().isSuccess) return@launch

                            val init = repo.getUserInitSettings().getOrNull()
                            val needNameTag =
                                init?.needsTagSetup == true || isNewUser

                            if (needNameTag) {
                                navController.navigate("onboarding_name") {
                                    popUpTo("onboarding_policy") { inclusive = true }
                                }
                            } else {
                                navController.navigate("main") {
                                    popUpTo("onboarding_policy") { inclusive = true }
                                }
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "전체 동의 후 시작하기",
                    color = Color(0xFF17181B),
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (selectedPolicyType != null) {
        val content = getOnboardingPolicyContent(selectedPolicyType!!)
        PolicyDetailDialog(
            title = content.title,
            body = content.body,
            onDismiss = { selectedPolicyType = null }
        )
    }
}

@Composable
private fun AgreementRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    onViewFullClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFC4F236),
                uncheckedColor = Color(0xFFC4F236),
                checkmarkColor = Color(0xFF17181B)
            )
        )
        Text(
            text = label,
            color = Color(0xFFEBEBEE),
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "전문보기",
            color = Color(0xFF9AA03C),
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onViewFullClick() }
        )
    }
}

@Composable
private fun PolicyDetailDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .background(Color(0xFF2D2D2F), RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-10).dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(text = "X", color = Color.White, fontSize = 20.sp)
            }

            Column(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                PolicyFormattedBody(body = body)
            }
        }
    }
}

@Composable
private fun PolicyFormattedBody(body: String) {
    val sections = remember(body) { splitPolicySections(body) }
    if (sections.isEmpty()) {
        Text(
            text = body.trim(),
            color = Color(0xFFE9E9EA),
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        return
    }
    Column {
        sections.forEachIndexed { index, section ->
            Text(
                text = section.title,
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            val formatted = formatSectionLines(section.lines)
            formatted.forEach { line ->
                when (line) {
                    is PolicyBodyLine.Plain -> {
                        Text(
                            text = line.text,
                            color = Color(0xFFE9E9EA),
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    is PolicyBodyLine.Numbered -> {
                        Text(
                            text = "${line.index}. ${line.text}",
                            color = Color(0xFFE9E9EA),
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    PolicyBodyLine.Blank -> Spacer(modifier = Modifier.height(10.dp))
                }
            }
            if (index < sections.lastIndex) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
