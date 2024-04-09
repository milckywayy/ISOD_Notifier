package pl.edu.pw.ee.isod_notifier.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.edu.pw.ee.isod_notifier.ui.screens.activities.ClassesScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.activities.EventsScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.activities.NewsScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.activities.ScheduleScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.auth.FirstTimeLinkScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.auth.LinkIsodScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.auth.LinkUsosScreen

@Composable
fun AppNavHost(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = "home") {
        // Main
        composable("home") { HomeScreen(navHostController) }
        composable("settings") { SettingsScreen(navHostController) }

        // Auth
        composable("first_time_link_screen") { FirstTimeLinkScreen(navHostController) }
        composable("link_isod") { LinkIsodScreen(navHostController) }
        composable("link_usos") { LinkUsosScreen(navHostController) }

        // Activities
        composable("classes") { ClassesScreen(navHostController) }
        composable("events") { EventsScreen(navHostController) }
        composable("news") { NewsScreen(navHostController) }
        composable("schedule") { ScheduleScreen(navHostController) }
    }
}