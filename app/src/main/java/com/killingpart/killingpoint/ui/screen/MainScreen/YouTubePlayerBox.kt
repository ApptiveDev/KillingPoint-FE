package com.killingpart.killingpoint.ui.screen.MainScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import androidx.compose.runtime.DisposableEffect

/**
 * YouTube Player API를 사용한 백그라운드 재생 가능한 플레이어
 */
@Composable
fun YouTubePlayerBox(
    diary: Diary?, 
    startSeconds: Float, 
    durationSeconds: Float = 0f,
    onVideoReady: () -> Unit = {},
    isPlayingState: Boolean? = null
) {
    val context = LocalContext.current
    
    // videoUrl이 변경되면 player 상태도 초기화되도록 key 사용
    val currentVideoUrl = diary?.videoUrl ?: ""
    
    var isPlaying by remember(currentVideoUrl) { mutableStateOf(false) }
    var player by remember(currentVideoUrl) { mutableStateOf<YouTubePlayer?>(null) }
    var currentTime by remember(currentVideoUrl) { mutableStateOf(0f) }
    
    // 콜백을 remember로 저장하여 리스너에서 사용
    val videoReadyCallback = remember(onVideoReady) { onVideoReady }
    
    val endSeconds = if (durationSeconds > 0f) {
        startSeconds + durationSeconds
    } else {
        null
    }

    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (diary != null) {
            val videoId = if (diary.videoUrl.contains("/embed/")) {
                diary.videoUrl.substringAfter("/embed/").substringBefore("?")
            } else {
                diary.videoUrl.substringBefore("?")
            }
            
            LaunchedEffect(diary.videoUrl) {
                android.util.Log.d("YouTubePlayerBox", "YouTubePlayerBox 초기화: videoUrl=${diary.videoUrl}, videoId=$videoId")
            }
            
            // startSeconds가 변경될 때 seekTo로 위치 이동 (디바운싱 적용)
            LaunchedEffect(startSeconds) {
                if (player != null) {
                    // 500ms 디바운싱: 사용자가 슬라이더를 계속 움직이면 마지막 값만 적용
                    kotlinx.coroutines.delay(500)
                    player?.seekTo(startSeconds)
                }
            }

            LaunchedEffect(currentTime, endSeconds, startSeconds, durationSeconds, player) {
                if (isPlaying && player != null && endSeconds != null && durationSeconds > 0f && currentTime >= endSeconds) {
                    player?.seekTo(startSeconds)
                }
            }

            LaunchedEffect(isPlayingState, player) {
                if (player != null && isPlayingState != null) {
                    if (isPlayingState) {
                        player?.play()
                    } else {
                        player?.pause()
                    }
                }
            }

            LaunchedEffect(diary.videoUrl) {
                player?.pause()
                player = null
                isPlaying = false
                currentTime = 0f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                key(diary.videoUrl) {

                    var playerView by remember { mutableStateOf<YouTubePlayerView?>(null) }

                    DisposableEffect(diary.videoUrl) {
                        onDispose {
                            player?.pause()
                            player = null
                            isPlaying = false
                            currentTime = 0f
                            playerView?.release()
                            playerView = null
                        }
                    }
                    
                    AndroidView(
                        factory = { context ->
                            player?.pause()
                            player = null
                            playerView?.release()
                            
                            YouTubePlayerView(context).apply {
                                playerView = this
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                var hasCalledReady = false
                                
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    android.util.Log.d("YouTubePlayerBox", "onReady 호출: videoId=$videoId, startSeconds=$startSeconds")
                                    player = youTubePlayer
                                    hasCalledReady = false
                                    youTubePlayer.loadVideo(videoId, startSeconds)
                                    youTubePlayer.play()
                                    isPlaying = true
                                    android.util.Log.d("YouTubePlayerBox", "비디오 로드 및 재생 시작")
                                }
                                
                                override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                                    super.onStateChange(youTubePlayer, state)
                                    android.util.Log.d("YouTubePlayerBox", "onStateChange: state=$state")
                                    when (state) {
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                                            android.util.Log.d("YouTubePlayerBox", "비디오 재생 중")
                                            isPlaying = true
                                            if (!hasCalledReady) {
                                                hasCalledReady = true
                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                    videoReadyCallback()
                                                }, 500)
                                            }
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                                            android.util.Log.d("YouTubePlayerBox", "비디오 일시정지")
                                            isPlaying = false
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED -> {
                                            android.util.Log.d("YouTubePlayerBox", "비디오 종료")
                                            isPlaying = false
                                            if (endSeconds == null || durationSeconds == 0f) {
                                                youTubePlayer.seekTo(startSeconds)
                                                youTubePlayer.play()
                                            }
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.UNSTARTED -> {
                                            android.util.Log.d("YouTubePlayerBox", "비디오 시작 전")
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.BUFFERING -> {
                                            android.util.Log.d("YouTubePlayerBox", "비디오 버퍼링 중")
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.VIDEO_CUED -> {
                                            android.util.Log.d("YouTubePlayerBox", "비디오 큐됨")
                                        }
                                        else -> {
                                            android.util.Log.d("YouTubePlayerBox", "알 수 없는 상태: $state")
                                        }
                                    }
                                }
                                
                                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                    super.onCurrentSecond(youTubePlayer, second)
                                    currentTime = second
                                }
                            })
                        }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            diary.musicTitle?.let { title ->
                Text(
                    text = title,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            diary.artist?.let { artist ->
                Text(
                    text = artist,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

        }
    }
}
