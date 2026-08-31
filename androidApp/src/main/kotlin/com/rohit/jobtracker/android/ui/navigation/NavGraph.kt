package com.rohit.jobtracker.android.ui.navigation

sealed class NavRoute(val route: String) {
    data object List : NavRoute("applications")
    data object Add : NavRoute("applications/add")
    data object Edit : NavRoute("applications/edit/{applicationId}") {
        fun createRoute(applicationId: String) = "applications/edit/$applicationId"
    }
    data object Detail : NavRoute("applications/{applicationId}") {
        fun createRoute(applicationId: String) = "applications/$applicationId"
    }
}
