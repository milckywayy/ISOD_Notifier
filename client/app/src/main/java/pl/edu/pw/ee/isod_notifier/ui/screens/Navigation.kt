package pl.edu.pw.ee.isod_notifier.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.edu.pw.ee.isod_notifier.ui.screens.activities.*
import pl.edu.pw.ee.isod_notifier.ui.screens.auth.FirstTimeLinkScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.auth.LinkIsodScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.auth.LinkUsosScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.settings.AppInfoScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.settings.ConnectionErrorScreen
import pl.edu.pw.ee.isod_notifier.ui.screens.settings.SettingsScreen

enum class ScaleTransitionDirection {
    INWARDS, OUTWARDS
}

fun scaleIntoContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
    initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
): EnterTransition {
    return scaleIn(
        animationSpec = tween(220, delayMillis = 90),
        initialScale = initialScale
    ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
}

fun scaleOutOfContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
    targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition {
    return scaleOut(
        animationSpec = tween(
            durationMillis = 220,
            delayMillis = 90
        ), targetScale = targetScale
    ) + fadeOut(tween(delayMillis = 90))
}

@Composable
fun AppNavHost(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = "first_time_link_screen") {
        // Main
        composable("home",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { HomeScreen(navHostController) }

        // Settings
        composable("settings",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { SettingsScreen(navHostController) }

        composable("app_info",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { AppInfoScreen(navHostController) }

        composable("connection_error",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { ConnectionErrorScreen(navHostController) }

        // Auth
        composable("first_time_link_screen",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { FirstTimeLinkScreen(navHostController) }

        composable("link_isod",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { LinkIsodScreen(navHostController) }

        composable("link_usos",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { LinkUsosScreen(navHostController) }

        // Activities
        composable("classes",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { ClassesScreen(navHostController) }

        composable("events",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { EventsScreen(navHostController) }

        composable("news",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { NewsScreen(navHostController) }

        composable("newsInfo/{hash}/{service}",
            arguments = listOf(
                navArgument("hash") { type = NavType.StringType },
                navArgument("service") { type = NavType.StringType },
            ),
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { backStackEntry ->
            val hash = backStackEntry.arguments?.getString("hash")
            val service = backStackEntry.arguments?.getString("service")
            if (hash != null && service != null) {
                NewsInfoScreen(navHostController, hash, service)
            }
        }

        composable("grades/{course_id}/{class_type}/{term}",
            arguments = listOf(
                navArgument("course_id") { type = NavType.StringType },
                navArgument("class_type") { type = NavType.StringType },
                navArgument("term") { type = NavType.StringType },
            ),
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("course_id")
            val classType = backStackEntry.arguments?.getString("class_type")
            val term = backStackEntry.arguments?.getString("term")
            if (courseId != null && classType != null && term != null) {
                GradeScreen(navHostController, courseId, classType, term)
            }
        }

        composable("schedule",
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() }
        ) { ScheduleScreen(navHostController) }
    }
}