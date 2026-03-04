package com.killingpart.killingpoint.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.screen.HomeScreen.HelloScreen
import com.killingpart.killingpoint.ui.screen.MainScreen.MainScreen
import com.killingpart.killingpoint.ui.screen.TutorialScreen.TutorialScreen
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.AddMusicScreen
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.WriteDiaryScreen
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.SelectDurationScreen
import com.killingpart.killingpoint.ui.screen.DiaryDetailScreen.DiaryDetailScreen
import com.killingpart.killingpoint.ui.screen.DiaryDetailScreen.DiaryDetailScreenForStored
import com.killingpart.killingpoint.ui.screen.SocialScreen.SocialScreen
import com.killingpart.killingpoint.ui.screen.SocialScreen.FriendProfileScreen
import com.killingpart.killingpoint.ui.screen.SocialScreen.PickFandomListScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.killingpart.killingpoint.ui.screen.SearchScreen.SearchScreen
import java.net.URLDecoder

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
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
                    "&fromTab={fromTab}" +
                    "&authorUsername={authorUsername}" +
                    "&authorTag={authorTag}",
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
                navArgument("fromTab") { type = NavType.StringType; defaultValue = "" },
                navArgument("authorUsername") { type = NavType.StringType; defaultValue = "" },
                navArgument("authorTag") { type = NavType.StringType; defaultValue = "" }
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
            val authorUsername = URLDecoder.decode(backStackEntry.arguments?.getString("authorUsername").orEmpty(), "UTF-8")
            val authorTag = URLDecoder.decode(backStackEntry.arguments?.getString("authorTag").orEmpty(), "UTF-8")

            if (fromTab == "stored") {
                DiaryDetailScreenForStored(
                    navController = navController,
                    artist = artist,
                    musicTitle = musicTitle,
                    albumImageUrl = albumImageUrl,
                    videoUrl = videoUrl,
                    duration = duration,
                    start = start,
                    end = end,
                    createDate = createDate,
                    totalDuration = totalDuration,
                    diaryId = diaryId
                )
            } else {
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
                    fromTab = fromTab,
                    authorUsername = authorUsername,
                    authorTag = authorTag
                )
            }
        }

        composable(
            route = "social?tab={tab}",
            arguments = listOf(
                navArgument("tab") { type = NavType.StringType; defaultValue = "feed" }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "feed"
            SocialScreen(navController, tab)
        }

        composable(
            route = "friend_profile" +
                    "?userId={userId}" +
                    "&username={username}" +
                    "&tag={tag}" +
                    "&profileImageUrl={profileImageUrl}" +
                    "&isMyPick={isMyPick}" +
                    "&fromPickFandomList={fromPickFandomList}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("username") { type = NavType.StringType; defaultValue = "" },
                navArgument("tag") { type = NavType.StringType; defaultValue = "" },
                navArgument("profileImageUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("isMyPick") { type = NavType.BoolType; defaultValue = false },
                navArgument("fromPickFandomList") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val username = URLDecoder.decode(backStackEntry.arguments?.getString("username").orEmpty(), "UTF-8")
            val tag = URLDecoder.decode(backStackEntry.arguments?.getString("tag").orEmpty(), "UTF-8")
            val profileImageUrl = URLDecoder.decode(backStackEntry.arguments?.getString("profileImageUrl").orEmpty(), "UTF-8")
            val isMyPick = backStackEntry.arguments?.getBoolean("isMyPick") ?: false
            val fromPickFandomList = backStackEntry.arguments?.getBoolean("fromPickFandomList") ?: false

            FriendProfileScreen(
                navController = navController,
                userId = userId,
                username = username,
                tag = tag,
                profileImageUrl = profileImageUrl,
                isMyPick = isMyPick,
                fromPickFandomList = fromPickFandomList
            )
        }
        composable("search") {
            SearchScreen(navController)
        }

        composable(
            route = "pick_fandom_list" +
                    "?userId={userId}" +
                    "&tag={tag}" +
                    "&initialTab={initialTab}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("tag") { type = NavType.StringType; defaultValue = "" },
                navArgument("initialTab") { type = NavType.StringType; defaultValue = "picks" }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val tag = URLDecoder.decode(backStackEntry.arguments?.getString("tag").orEmpty(), "UTF-8")
            val initialTab = URLDecoder.decode(
                backStackEntry.arguments?.getString("initialTab").orEmpty(),
                "UTF-8"
            )
            PickFandomListScreen(
                navController = navController,
                userId = userId,
                tag = tag,
                initialTab = initialTab
            )
        }
    }
}
