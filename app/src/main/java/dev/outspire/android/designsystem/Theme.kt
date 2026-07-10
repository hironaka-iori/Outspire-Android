package dev.outspire.android.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Brand = androidx.compose.ui.graphics.Color(0xFF6650A4)
private val BrandDark = androidx.compose.ui.graphics.Color(0xFFD0BCFF)
private val RichDarkBackground = androidx.compose.ui.graphics.Color(0xFF0F0F17)
private val RichDarkSurface = androidx.compose.ui.graphics.Color(0xFF191923)

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = androidx.compose.ui.graphics.Color(0xFF52606D),
    tertiary = androidx.compose.ui.graphics.Color(0xFF006D75),
    background = androidx.compose.ui.graphics.Color(0xFFF8F7FC),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFBFF),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE8E5EE),
)

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    secondary = androidx.compose.ui.graphics.Color(0xFFBEC6D0),
    tertiary = androidx.compose.ui.graphics.Color(0xFF82D3DB),
    background = RichDarkBackground,
    surface = RichDarkSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF22222E),
)

private val OutspireTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
)

@Composable
fun OutspireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = OutspireTypography,
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.sm),
            small = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.md),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.lg),
            large = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.card),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.xl),
        ),
        content = content,
    )
}
