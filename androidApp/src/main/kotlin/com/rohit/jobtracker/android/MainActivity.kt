package com.rohit.jobtracker.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JobTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JobTrackerApp()
                }
            }
        }
    }
}

@Composable
fun JobTrackerApp() {
    val navController = rememberNavController()

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
            route = NavRoute.Detail.route,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            ApplicationDetailScreen(
                applicationId = applicationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
