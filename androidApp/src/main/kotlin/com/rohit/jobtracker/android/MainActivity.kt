package com.rohit.jobtracker.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rohit.jobtracker.android.ui.addedit.AddEditApplicationScreen
import com.rohit.jobtracker.android.ui.detail.ApplicationDetailScreen
import com.rohit.jobtracker.android.ui.list.ApplicationListScreen
import com.rohit.jobtracker.android.ui.navigation.NavRoute
import com.rohit.jobtracker.android.ui.theme.JobTrackerTheme
import com.rohit.jobtracker.android.ui.theme.ThemeConfig
import com.rohit.jobtracker.android.ui.theme.ThemeMode
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val themeConfig: ThemeConfig by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val targetApplicationId = intent.getStringExtra("applicationId")

        setContent {
            val themeMode by themeConfig.themeMode.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            JobTrackerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JobTrackerApp(initialApplicationId = targetApplicationId)
                }
            }
        }
    }
}

@Composable
fun JobTrackerApp(initialApplicationId: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(initialApplicationId) {
        if (!initialApplicationId.isNullOrBlank()) {
            navController.navigate(NavRoute.Detail.createRoute(initialApplicationId))
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoute.List.route
    ) {
        composable(NavRoute.List.route) {
            ApplicationListScreen(
                onNavigateToAdd = { navController.navigate(NavRoute.Add.route) },
                onNavigateToDetail = { id -> navController.navigate(NavRoute.Detail.createRoute(id)) }
            )
        }

        composable(NavRoute.Add.route) {
            AddEditApplicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoute.Edit.route,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            AddEditApplicationScreen(
                applicationId = applicationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoute.Detail.route,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            ApplicationDetailScreen(
                applicationId = applicationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(NavRoute.Edit.createRoute(id)) }
            )
        }
    }
}
