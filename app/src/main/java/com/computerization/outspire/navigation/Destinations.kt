package com.computerization.outspire.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val LOGIN = "login"
    const val TODAY = "today"
    const val ACADEMIC = "academic"
    const val CAS = "cas"
    const val SETTINGS = "settings"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    TODAY(Routes.TODAY, "Today", Icons.Outlined.Home),
    ACADEMIC(Routes.ACADEMIC, "Academic", Icons.Outlined.Book),
    CAS(Routes.CAS, "CAS", Icons.Outlined.Star),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings),
}
