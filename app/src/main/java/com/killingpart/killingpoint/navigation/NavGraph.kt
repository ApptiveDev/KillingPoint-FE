package com.killingpart.killingpoint.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.screen.HomeScreen.HelloScreen
import com.killingpart.killingpoint.ui.screen.MainScreen.MainScreen
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.AddMusicScreen
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.WriteDiaryScreen
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.SelectDurationScreen
import com.killingpart.killingpoint.ui.screen.DiaryDetailScreen.DiaryDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import java.net.URLDecoder

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HelloScreen(navController) }

        composable(
            route = "main?tab={tab}&selectedDate={selectedDate}",
            arguments = listOf(
                navArgument("tab") { type = NavType.StringType; defaultValue = "play" },
                navArgument("selectedDate") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "play"
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
            MainScreen(navController, tab, selectedDate)
        }

        composable("add_music") {
            AddMusicScreen(navController)
        }

        composable(
            route = "select_duration" +
                    "?title={title}" +
                    "&artist={artist}" +
                    "&image={image}" +
                    "&videoUrl={videoUrl}" +
                    "&totalDuration={totalDuration}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("artist") { type = NavType.StringType; defaultValue = "" },
                navArgument("image") { type = NavType.StringType; defaultValue = "" },
                navArgument("videoUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("totalDuration") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title").orEmpty(), "UTF-8")
            val artist = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("artist").orEmpty(), "UTF-8")
            val image = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("image").orEmpty(), "UTF-8")
            val videoUrl = URLDecoder.decode(backStackEntry.arguments?.getString("videoUrl").orEmpty(), "UTF-8")
            val totalDurationStr = backStackEntry.arguments?.getString("totalDuration") ?: ""
            val totalDuration = totalDurationStr.toIntOrNull() ?: 0

            SelectDurationScreen(navController, title, artist, image, videoUrl, totalDuration)
        }

        composable(
            route = "write_diary" +
                    "?title={title}" +
                    "&artist={artist}" +
                    "&image={image}" +
                    "&duration={duration}" +
                    "&start={start}" +
                    "&end={end}" +
                    "&videoUrl={videoUrl}" +
                    "&totalDuration={totalDuration}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("artist") { type = NavType.StringType; defaultValue = "" },
                navArgument("image") { type = NavType.StringType; defaultValue = "" },
                navArgument("duration") { type = NavType.StringType; defaultValue = "" },
                navArgument("start") { type = NavType.StringType; defaultValue = "" },
                navArgument("end") { type = NavType.StringType; defaultValue = "" },
                navArgument("videoUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("totalDuration") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title").orEmpty(), "UTF-8")
            val artist = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("artist").orEmpty(), "UTF-8")
            val image = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("image").orEmpty(), "UTF-8")
            val duration = URLDecoder.decode(backStackEntry.arguments?.getString("duration").orEmpty(), "UTF-8")
            val start = URLDecoder.decode(backStackEntry.arguments?.getString("start").orEmpty(), "UTF-8")
            val end = URLDecoder.decode(backStackEntry.arguments?.getString("end").orEmpty(), "UTF-8")
            val videoUrl = URLDecoder.decode(backStackEntry.arguments?.getString("videoUrl").orEmpty(), "UTF-8")
            val totalDurationStr = backStackEntry.arguments?.getString("totalDuration") ?: ""
            val totalDuration = totalDurationStr.toIntOrNull() ?: 0

            WriteDiaryScreen(navController, title, artist, image, duration, start, end, videoUrl, totalDuration)
        }

        composable(
            route = "diary_detail" +
                    "?artist={artist}" +
                    "&musicTitle={musicTitle}" +
                    "&albumImageUrl={albumImageUrl}" +
                    "&content={content}" +
                    "&videoUrl={videoUrl}" +
                    "&duration={duration}" +
                    "&start={start}" +
                    "&end={end}" +
                    "&createDate={createDate}" +
                    "&selectedDate={selectedDate}" +
                    "&scope={scope}" +
                    "&diaryId={diaryId}" +
                    "&totalDuration={totalDuration}" +
                    "&fromTab={fromTab}",
            arguments = listOf(
                navArgument("artist") { type = NavType.StringType; defaultValue = "" },
                navArgument("musicTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("albumImageUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("content") { type = NavType.StringType; defaultValue = "" },
                navArgument("videoUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("duration") { type = NavType.StringType; defaultValue = "" },
                navArgument("start") { type = NavType.StringType; defaultValue = "" },
                navArgument("end") { type = NavType.StringType; defaultValue = "" },
                navArgument("createDate") { type = NavType.StringType; defaultValue = "" },
                navArgument("selectedDate") { type = NavType.StringType; defaultValue = "" },
                navArgument("scope") { type = NavType.StringType; defaultValue = "" },
                navArgument("diaryId") { type = NavType.StringType; defaultValue = "" },
                navArgument("totalDuration") { type = NavType.StringType; defaultValue = "" },
                navArgument("fromTab") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val artist = URLDecoder.decode(backStackEntry.arguments?.getString("artist").orEmpty(), "UTF-8")
            val musicTitle = URLDecoder.decode(backStackEntry.arguments?.getString("musicTitle").orEmpty(), "UTF-8")
            val albumImageUrl = URLDecoder.decode(backStackEntry.arguments?.getString("albumImageUrl").orEmpty(), "UTF-8")
            val content = URLDecoder.decode(backStackEntry.arguments?.getString("content").orEmpty(), "UTF-8")
            val videoUrl = URLDecoder.decode(backStackEntry.arguments?.getString("videoUrl").orEmpty(), "UTF-8")
            val duration = URLDecoder.decode(backStackEntry.arguments?.getString("duration").orEmpty(), "UTF-8")
            val start = URLDecoder.decode(backStackEntry.arguments?.getString("start").orEmpty(), "UTF-8")
            val end = URLDecoder.decode(backStackEntry.arguments?.getString("end").orEmpty(), "UTF-8")
            val createDate = URLDecoder.decode(backStackEntry.arguments?.getString("createDate").orEmpty(), "UTF-8")
            val selectedDate = URLDecoder.decode(backStackEntry.arguments?.getString("selectedDate").orEmpty(), "UTF-8")
            val scope = URLDecoder.decode(backStackEntry.arguments?.getString("scope").orEmpty(), "UTF-8")
            val diaryIdStr = backStackEntry.arguments?.getString("diaryId") ?: ""
            val diaryId = diaryIdStr.toLongOrNull()
            val totalDurationStr = backStackEntry.arguments?.getString("totalDuration") ?: ""
            val totalDuration = totalDurationStr.toIntOrNull()
            android.util.Log.d("NavGraph", "diary_detail - totalDurationStr: '$totalDurationStr', totalDuration: $totalDuration")
            val fromTab = URLDecoder.decode(backStackEntry.arguments?.getString("fromTab").orEmpty(), "UTF-8")

            DiaryDetailScreen(
                navController = navController,
                artist = artist,
                musicTitle = musicTitle,
                albumImageUrl = albumImageUrl,
                content = content,
                videoUrl = videoUrl,
                duration = duration,
                start = start,
                end = end,
                createDate = createDate,
                selectedDate = selectedDate,
                scope = scope,
                diaryId = diaryId,
                totalDuration = totalDuration,
                fromTab = fromTab
            )
        }
    }
}
