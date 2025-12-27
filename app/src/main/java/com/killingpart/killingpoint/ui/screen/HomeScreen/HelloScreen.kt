package com.killingpart.killingpoint.ui.screen.HomeScreen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
                if (!state.isNew) {
                    navController.navigate("main") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            else -> {
                // 다른 상태는 처리x
            }
        }
    }

    Column (
        modifier = Modifier.fillMaxSize()
            .background(color = mainGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Column (
            horizontalAlignment = Alignment.Start
        ){
            Image(
                painter = painterResource(id = R.drawable.killingpart_logo),
                contentDescription = "KillingPart Logo",
                modifier = Modifier.size(width = 300.dp, height = 78.dp)
                    .clickable{onTestLoginClick(context, loginViewModel)}
            )

            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.killingpart_logo1),
                contentDescription = "KillingPart Logo Desc",
                modifier = Modifier.size(234.dp, 33.dp)
            )
        }


        Spacer(modifier = Modifier.height(182.dp))

        Text(
            text = "SNS로 간편 로그인",
            color = textGray1,
            fontSize = 12.sp,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.size(240.dp, 54.dp)
                .background(color = Color(0xFF1D1E20), RoundedCornerShape(100))
                .clickable {onSnsLoginClick(context, loginViewModel)},
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

        Spacer(modifier = Modifier.height(7.dp))

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
