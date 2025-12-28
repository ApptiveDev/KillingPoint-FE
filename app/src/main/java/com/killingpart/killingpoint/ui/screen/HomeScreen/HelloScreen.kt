package com.killingpart.killingpoint.ui.screen.HomeScreen

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.auth.KakaoLoginClient
import com.killingpart.killingpoint.ui.theme.darkGray
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.theme.textGray1
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState

@Composable
fun HelloScreen(navController: NavController) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        loginViewModel.tryAutoLogin(context)
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUiState.AutoLoginSuccess -> {
                navController.navigate("main") {
                    popUpTo("home") { inclusive = true }
                }
            }

            else -> {
                // 다른 상태는 처리x
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 배경 비디오
        BackgroundVideo(
            videoFileName = "login_video.mp4",
            modifier = Modifier.fillMaxSize()
        )

        // 2. 로고 영역 (화면 상단 왼쪽에 고정)
        // 부모의 Center 영향을 받지 않도록 별도로 빼기
        Column(
            modifier = Modifier
                .align(Alignment.TopStart) // 화면 좌상단 정렬
                .padding(top = 200.dp, start = 20.dp) // 원하는 만큼 위치 조정 (여기만 수정하면 로고 위치 이동)
        ) {
            Image(
                painter = painterResource(id = R.drawable.killing_part_logo_white2),
                contentDescription = "KillingPart Logo",
                modifier = Modifier
                    .size(width = 320.dp, height = 110.dp)
                    .clickable { onTestLoginClick(context, loginViewModel) }
            )
        }

        // 3. 로그인 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter) // 화면 하단 중앙 정렬
                .padding(bottom = 100.dp), // 바닥에서 얼마나 띄울지 결정
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SNS로 간편 로그인",
                color = textGray1,
                fontSize = 12.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .size(240.dp, 54.dp)
                    .background(color = Color(0xFF1D1E20), shape = RoundedCornerShape(100))
                    .clickable { onSnsLoginClick(context, loginViewModel) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao),
                    contentDescription = "KakaoLogin",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "카카오 로그인",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFFFEE500)
                )
            }
        }
    }

//        Row(
//            modifier = Modifier.size(240.dp, 54.dp)
//                .background(color = Color(0xFF1D1E20), RoundedCornerShape(100))
//                .clickable {onSnsLoginClick(context, loginViewModel)},
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.google),
//                contentDescription = "GoogleLogin",
//                modifier = Modifier.size(14.dp)
//            )
//            Spacer(modifier = Modifier.width(14.dp))
//            Text(
//                text = "구글 로그인",
//                fontFamily = PaperlogyFontFamily,
//                fontWeight = FontWeight.Normal,
//                fontSize = 14.sp,
//                color = Color.White
//            )
//        }
}

@OptIn(UnstableApi::class)
@Composable
fun BackgroundVideo(
    videoFileName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(
                Uri.parse("asset:///$videoFileName")
            )
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL // 반복 재생
            volume = 0f // 음소거 (필요시 조정)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // 화면을 꽉 채우도록
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun onSnsLoginClick(context: Context, loginViewModel: LoginViewModel) {
    loginViewModel.loginWithKakao(context) { kakaoAccessToken ->
        loginViewModel.loginWithServer(context, kakaoAccessToken)
    }
}

private fun onTestLoginClick(context: Context, loginViewModel: LoginViewModel) {
    loginViewModel.loginWithTest(context)
}

@Preview
@Composable
fun loginPreview() {
    HelloScreen(navController = rememberNavController())
}
