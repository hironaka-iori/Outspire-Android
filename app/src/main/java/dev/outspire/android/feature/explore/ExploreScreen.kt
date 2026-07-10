package dev.outspire.android.feature.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppRadius
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.RichCard
import dev.outspire.android.designsystem.ScreenTitle

@Composable
fun ExploreScreen(
    user: User?,
    onToday: () -> Unit,
    onClasses: () -> Unit,
    onActivities: () -> Unit,
    onGrades: () -> Unit,
    onAccount: () -> Unit,
    onPlaceholder: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.padding(top = AppSpace.xs))
        ScreenTitle("Explore", "Campus tools and information")

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            ExploreTile(
                "Today",
                Icons.Default.Today,
                listOf(Color(0xFF6650A4), Color(0xFF8068B1)),
                onToday,
                Modifier.weight(1f),
            )
            ExploreTile(
                "Classes",
                Icons.Default.CalendarMonth,
                listOf(Color(0xFF3F51B5), Color(0xFF6573C3)),
                onClasses,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            ExploreTile(
                "Activities",
                Icons.Default.Badge,
                listOf(Color(0xFF2E7D5A), Color(0xFF4B9473)),
                onActivities,
                Modifier.weight(1f),
            )
            ExploreTile(
                "Grades",
                Icons.Default.School,
                listOf(Color(0xFFCF6C25), Color(0xFFDF8A4D)),
                onGrades,
                Modifier.weight(1f),
            )
        }

        RichCard {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
                ExploreRow("School Arrangements", Icons.Default.CalendarMonth) { onPlaceholder("School Arrangements") }
                ExploreRow("Dining Menus", Icons.Default.Restaurant) { onPlaceholder("Dining Menus") }
                ExploreRow("Hall of Clubs", Icons.Default.Groups) { onPlaceholder("Hall of Clubs") }
                ExploreRow("Reflections", Icons.Default.MenuBook) { onPlaceholder("Reflections") }
                ExploreRow(if (user == null) "Account" else user.name, Icons.Default.Person, onAccount)
                ExploreRow("Settings", Icons.Default.Settings) { onPlaceholder("Settings") }
            }
        }
        Spacer(Modifier.padding(bottom = 88.dp))
    }
}

@Composable
private fun ExploreTile(
    label: String,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Brush.linearGradient(colors), RoundedCornerShape(AppRadius.lg))
            .clickable(onClick = onClick)
            .padding(AppSpace.md),
        verticalArrangement = Arrangement.spacedBy(AppSpace.lg),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ExploreRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppSpace.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpace.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.md),
    ) {
        ScreenTitle(title)
        RichCard {
            Text(
                "This feature is defined in the migration plan and will be connected to its original Swift implementation in a later milestone.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
