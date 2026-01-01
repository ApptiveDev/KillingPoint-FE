package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.regex.Pattern
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.focus.focusModifier
import androidx.activity.compose.BackHandler
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ProfileSettingsScreen(
    onDismiss: () -> Unit,
    topOffset: androidx.compose.ui.unit.Dp = 0.dp,
    maxHeight: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    
    // 모달 상태를 최상위 레벨로 이동
    var showLogoutModal by remember { mutableStateOf(false) }
    var showUnregisterModal by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }
    
    // 로그아웃/회원탈퇴 모달이 열려있을 때 뒤로가기 처리
    BackHandler(enabled = showLogoutModal || showUnregisterModal) {
        if (showLogoutModal) {
            showLogoutModal = false
        }
        if (showUnregisterModal) {
            showUnregisterModal = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 반투명 배경 (배경 클릭 시 닫기)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (maxHeight != androidx.compose.ui.unit.Dp.Unspecified) {
                        Modifier.height(maxHeight)
                    } else {
                        Modifier.fillMaxHeight(0.85f)
                    }
                )
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .offset(y = topOffset),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDADADA))
        ) {
            ProfileSettingsContent(
                userState = userState,
                onDismiss = onDismiss,
                onTagUpdateSuccess = {
                    userViewModel.loadUserInfo(context)
                },
                onLogoutClick = { showLogoutModal = true },
                onUnregisterClick = { showUnregisterModal = true }
            )
        }
        
        // 로그아웃 모달 (Card 밖에서 렌더링)
        if (showLogoutModal) {
            LogoutUnregisterModal(
                title = "로그아웃을 진행합니다.",
                message = "로그아웃하시겠습니까?",
                confirmButtonText = "로그아웃하기",
                onConfirm = {
                    showLogoutModal = false
                    scope.launch {
                        try {
                            repo.logout()
                            onLogout() // 로그인 화면으로 이동
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileSettings", "로그아웃 실패: ${e.message}", e)
                        }
                    }
                },
                onDismiss = { showLogoutModal = false }
            )
        }
        
        // 회원탈퇴 모달 (Card 밖에서 렌더링)
        if (showUnregisterModal) {
            LogoutUnregisterModal(
                title = "회원탈퇴를 진행합니다.",
                message = "회원탈퇴시 기존 저장된 정보는 모두 초기화 됩니다.",
                confirmButtonText = "탈퇴하기",
                onConfirm = {
                    showUnregisterModal = false
                    scope.launch {
                        try {
                            repo.unregister()
                            onLogout() // 로그인 화면으로 이동
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileSettings", "회원탈퇴 실패: ${e.message}", e)
                        }
                    }
                },
                onDismiss = { showUnregisterModal = false }
            )
        }
    }
}

@Composable
private fun ProfileSettingsContent(
    userState: UserUiState,
    onDismiss: () -> Unit,
    onTagUpdateSuccess: () -> Unit,
    onLogoutClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    val userViewModel: UserViewModel = viewModel()
    
    var isEditingTag by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    
    // 프로필 이미지 업로드 상태
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    
    // 로그아웃/회원탈퇴 모달 상태
    var showLogoutModal by remember { mutableStateOf(false) }
    var showUnregisterModal by remember { mutableStateOf(false) }
    
    // Uri를 File로 변환하는 헬퍼 함수
    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            android.util.Log.e("ProfileSettings", "파일 변환 실패: ${e.message}")
            null
        }
    }
    
    // presignedUrl에서 쿼리파라미터 제거
    fun removeQueryParams(url: String): String {
        return url.split("?").first()
    }
    
    // 기본 이미지 URL
    val defaultProfileImageUrl = "https://killingpart-file.s3.ap-northeast-2.amazonaws.com/defaultImage/userDefaultImage.png"
    
    // 기본 이미지로 설정
    fun resetToDefaultImage() {
        if (isUploadingImage) return
        
        isUploadingImage = true
        imageUploadError = null
        
        scope.launch {
            try {
                // 기본 이미지의 경우도 presignedUrl 발급이 필요할 수 있지만,
                // 이미 존재하는 파일이므로 기본 이미지 URL을 직접 사용
                // 백엔드 API가 기본 이미지 URL을 직접 받을 수 있다면 이 방식으로,
                // 그렇지 않다면 presignedUrl 발급 후 기본 이미지 URL을 업로드해야 함
                
                // 1. PresignedUrl 발급
                val presignedUrlResult = repo.getPresignedUrl()
                val presignedUrlResponse = presignedUrlResult.getOrElse {
                    imageUploadError = it.message ?: "PresignedUrl 발급 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 2. 기본 이미지 URL을 presignedUrl로 업로드 (기본 이미지는 이미 S3에 있으므로 스킵 가능)
                // 하지만 API 구조상 presignedUrl이 필요하므로, 기본 이미지 URL을 직접 전달
                // 백엔드가 기본 이미지 URL을 처리할 수 있다면 이 방식으로
                
                // 3. 프로필 이미지를 기본 이미지 URL로 변경
                // 기본 이미지 URL을 presignedUrl처럼 사용 (백엔드가 이를 처리할 수 있어야 함)
                val updateResult = repo.updateProfileImage(
                    presignedUrlResponse.id,
                    defaultProfileImageUrl
                )
                updateResult.getOrElse {
                    imageUploadError = it.message ?: "기본 이미지로 설정 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 4. 성공 - 사용자 정보 새로고침
                userViewModel.loadUserInfo(context)
                onTagUpdateSuccess()
                imageUploadError = null
                
            } catch (e: Exception) {
                imageUploadError = e.message ?: "기본 이미지로 설정 실패"
                android.util.Log.e("ProfileSettings", "기본 이미지 설정 에러: ${e.message}", e)
            } finally {
                isUploadingImage = false
            }
        }
    }
    
    // 이미지 업로드 플로우
    fun uploadProfileImage(imageUri: Uri) {
        if (isUploadingImage) return
        
        isUploadingImage = true
        imageUploadError = null
        
        scope.launch {
            try {
                // 1. Uri를 File로 변환
                val imageFile = uriToFile(imageUri)
                if (imageFile == null) {
                    imageUploadError = "이미지 파일을 읽을 수 없습니다."
                    isUploadingImage = false
                    return@launch
                }
                
                // 2. PresignedUrl 발급
                val presignedUrlResult = repo.getPresignedUrl()
                val presignedUrlResponse = presignedUrlResult.getOrElse {
                    imageUploadError = it.message ?: "PresignedUrl 발급 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 3. S3에 이미지 업로드
                val uploadResult = repo.uploadImageToS3(
                    presignedUrlResponse.presignedUrl,
                    imageFile
                )
                uploadResult.getOrElse {
                    imageUploadError = it.message ?: "이미지 업로드 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 4. 쿼리파라미터 제거한 presignedUrl
                val cleanUrl = removeQueryParams(presignedUrlResponse.presignedUrl)
                
                // 5. 프로필 이미지 변경
                val updateResult = repo.updateProfileImage(
                    presignedUrlResponse.id,
                    cleanUrl
                )
                updateResult.getOrElse {
                    imageUploadError = it.message ?: "프로필 이미지 변경 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 6. 성공 - 사용자 정보 새로고침
                userViewModel.loadUserInfo(context)
                onTagUpdateSuccess()
                imageUploadError = null
                
            } catch (e: Exception) {
                imageUploadError = e.message ?: "프로필 이미지 업로드 실패"
                android.util.Log.e("ProfileSettings", "이미지 업로드 에러: ${e.message}", e)
            } finally {
                isUploadingImage = false
            }
        }
    }
    
    // 이미지 선택 launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            uploadProfileImage(it)
        }
    }
    
    // 이미지 선택 실행 함수
    fun pickImage() {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
    
    // 태그 유효성 검사
    fun validateTag(tag: String): String? {
        if (tag.isEmpty()) {
            return null
        }
        
        if (tag.length < 4 || tag.length > 30) {
            return "tag는 4자 이상 30자 이하이어야 합니다."
        }
        
        val pattern = Pattern.compile("^[a-z0-9_.]+$")
        if (!pattern.matcher(tag).matches()) {
            return "30자 이내의 영문과 숫자,특수문자([.],[_])로 조합해주세요."
        }
        
        return null
    }
    
    // 태그 편집 시작
    fun startEditingTag(currentTag: String) {
        tagText = currentTag
        isEditingTag = true
        validationMessage = null
    }
    
    // 태그 편집 취소
    fun cancelEditingTag() {
        isEditingTag = false
        tagText = ""
        validationMessage = null
    }
    
    // 태그 업데이트
    fun updateTag(originalTag: String) {
        if (isUpdating) return
        
        val validationError = validateTag(tagText)
        if (validationError != null) {
            validationMessage = validationError
            return
        }
        
        if (tagText == originalTag) {
            cancelEditingTag()
            return
        }
        
        isUpdating = true
        scope.launch {
            try {
                repo.updateTag(tagText)
                    .onSuccess {
                        userViewModel.loadUserInfo(context)
                        onTagUpdateSuccess()
                        cancelEditingTag()
                    }
                    .onFailure { e ->
                        val errorMessage = e.message ?: "태그 업데이트 실패"
                        
                        // 에러 메시지에서 JSON 추출 시도
                        try {
                            // "태그 업데이트 실패 (400): {JSON}" 형식에서 JSON 추출
                            val jsonStart = errorMessage.indexOf("{")
                            if (jsonStart != -1) {
                                val jsonStr = errorMessage.substring(jsonStart)
                                val json = JSONObject(jsonStr)
                                
                                // 1. message 필드 확인 (중복 검사 등)
                                if (json.has("message")) {
                                    validationMessage = json.getString("message")
                                    isUpdating = false
                                    return@launch
                                }
                                
                                // 2. fieldErrors 확인 (유효성 검사 에러)
                                if (json.has("fieldErrors")) {
                                    val fieldErrors = json.getJSONArray("fieldErrors")
                                    if (fieldErrors.length() > 0) {
                                        // 모든 fieldErrors의 tag 메시지를 합침
                                        val errorMessages = mutableListOf<String>()
                                        for (i in 0 until fieldErrors.length()) {
                                            val errorObj = fieldErrors.getJSONObject(i)
                                            if (errorObj.has("tag")) {
                                                errorMessages.add(errorObj.getString("tag"))
                                            }
                                        }
                                        if (errorMessages.isNotEmpty()) {
                                            validationMessage = errorMessages.joinToString("\n")
                                            isUpdating = false
                                            return@launch
                                        }
                                    }
                                }
                            }
                        } catch (parseException: Exception) {
                            android.util.Log.e("ProfileSettings", "JSON 파싱 실패: ${parseException.message}")
                            // JSON 파싱 실패 시 원본 메시지 사용
                        }
                        
                        // JSON 파싱 실패하거나 형식이 다른 경우 원본 에러 메시지 사용
                        validationMessage = errorMessage
                    }
                isUpdating = false
            } catch (e: Exception) {
                validationMessage = e.message ?: "태그 업데이트 실패"
                isUpdating = false
            }
        }
    }
    
    // 실시간 검증
    LaunchedEffect(tagText) {
        if (isEditingTag && tagText.isNotEmpty() && userState is UserUiState.Success) {
            val currentTag = (userState as UserUiState.Success).userInfo.tag
            if (tagText != currentTag) {
                validationMessage = validateTag(tagText)
            } else {
                validationMessage = null
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "프로필 설정",
                color = mainGreen,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 15.sp,


                )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "닫기",
                    tint = mainGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(15.dp))
        
        // 프로필 정보 영역
        when (val state = userState) {
            is UserUiState.Success -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    // 프로필 이미지 (클릭 가능)
                    Column() {
                        AsyncImage(
                            model = state.userInfo.profileImageUrl,
                            contentDescription = "프로필 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(50))
                                .border(3.dp, mainGreen, RoundedCornerShape(50))
                                .clickable(enabled = !isUploadingImage) {
                                    pickImage()
                                },
                            placeholder = painterResource(id = R.drawable.default_profile),
                            error = painterResource(id = R.drawable.default_profile)
                        )
                        
                        // 업로드 중 로딩 표시
                        if (isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = mainGreen,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = "프로필 사진",
                            color = mainGreen,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 11.sp
                        )
                        
                        // 기본 이미지로 설정 버튼
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable(
                                    enabled = !isUploadingImage && 
                                             state.userInfo.profileImageUrl != "https://killingpart-file.s3.ap-northeast-2.amazonaws.com/defaultImage/userDefaultImage.png"
                                ) {
                                    resetToDefaultImage()
                                },
                            text = "기본 이미지로 설정",
                            color = if (!isUploadingImage && 
                                       state.userInfo.profileImageUrl != "https://killingpart-file.s3.ap-northeast-2.amazonaws.com/defaultImage/userDefaultImage.png") {
                                Color(0xFF7B7B7B)
                            } else {
                                Color(0xFF4A4A4A)
                            },
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W400
                        )

                        // 이미지 업로드 에러 메시지
                        if (imageUploadError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = imageUploadError!!,
                                color = Color(0xFFFF6B6B),
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                maxLines = 2
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // username과 tag
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = state.userInfo.username,
                            color = mainGreen,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 17.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 태그 영역 (인라인 편집)
                        Column {
                            // 입력 필드 또는 표시 영역
                            if (isEditingTag) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFF1A1A1A),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "@ ",
                                        color = mainGreen,
                                        fontFamily = PaperlogyFontFamily,
                                        fontSize = 13.sp
                                    )
                                    
                                    androidx.compose.foundation.text.BasicTextField(

                                        value = tagText,
                                        onValueChange = { newValue ->
                                            tagText = newValue.lowercase()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = mainGreen,
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 13.sp
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                updateTag(state.userInfo.tag)
                                            }
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(mainGreen),

                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clickable { startEditingTag(state.userInfo.tag) }
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFF1A1A1A),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "@",
                                            color = mainGreen,
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = state.userInfo.tag,
                                            color = mainGreen,
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 13.sp
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "태그 수정",
                                        tint = mainGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            // 검증 메시지 영역 (입력 필드 바로 아래)
                            if (isEditingTag) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (validationMessage != null) {
                                    Text(
                                        text = validationMessage!!,
                                        color = if (validationMessage!!.contains("사용 가능") || validationMessage!!.contains("성공")) {
                                            Color(0xFF4FDD79)
                                        } else {
                                            Color(0xFFFF6B6B)
                                        },
                                        fontFamily = PaperlogyFontFamily,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, end = 10.dp)
                                    )
                                } else if (tagText.isNotEmpty() && tagText != state.userInfo.tag && validateTag(tagText) == null) {
                                    Text(
                                        text = "사용 가능한 회원태그입니다!",
                                        color = Color(0xFF4FDD79),
                                        fontFamily = PaperlogyFontFamily,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                }
                            }
                            
                            // 하단 버튼 영역
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditingTag) {
                                    if (isUpdating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = mainGreen,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { cancelEditingTag() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "취소",
                                                tint = mainGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        IconButton(
                                            onClick = { updateTag(state.userInfo.tag) },
                                            enabled = tagText.isNotEmpty() && 
                                                     tagText != state.userInfo.tag && 
                                                     validateTag(tagText) == null,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "저장",
                                                tint = if (tagText.isNotEmpty() && 
                                                          tagText != state.userInfo.tag && 
                                                          validateTag(tagText) == null) {
                                                    mainGreen
                                                } else {
                                                    Color.Gray
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            

                        }
                    }
                }

            }
            
            is UserUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "로딩 중...",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily
                    )
                }
            }
            
            is UserUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "오류: ${state.message}",
                        color = Color.Red,
                        fontFamily = PaperlogyFontFamily
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 로그아웃 / 회원탈퇴
        LogoutUnregisterSection(
            onLogoutClick = onLogoutClick,
            onUnregisterClick = onUnregisterClick
        )
    }
}

@Composable
private fun LogoutUnregisterSection(
    onLogoutClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }
    
    Text(
        text = "로그아웃 / 회원탈퇴",
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActionMenu = true }
            .padding(vertical = 20.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    
    if (showActionMenu) {
        ActionMenuDialog(
            onLogoutClick = {
                showActionMenu = false
                onLogoutClick()
            },
            onUnregisterClick = {
                showActionMenu = false
                onUnregisterClick()
            },
            onDismiss = { showActionMenu = false }
        )
    }
}

@Composable
private fun ActionMenuDialog(
    onLogoutClick: () -> Unit,
    onUnregisterClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        shape = RoundedCornerShape(20.dp),
        title = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(20.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "로그아웃",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogoutClick() }
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Divider(
                    color = Color(0xFF2A2A2A),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                
                Text(
                    text = "회원탈퇴",
                    color = Color(0xFFFF6B6B),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUnregisterClick() }
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun LogoutUnregisterModal(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
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
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // 메시지
                Text(
                    text = message,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 돌아가기 버튼 (흰색 배경, 검은색 텍스트, 테두리)
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Text(
                            text = "돌아가기",
                            color = Color.Black,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 확인 버튼 (빨간색 배경, 흰색 텍스트)
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = confirmButtonText,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

