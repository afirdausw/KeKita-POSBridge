package devyana.kekita.posbridge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Light Color Scheme ───────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = KeKitaPrimary,
    onPrimary           = KeKitaOnPrimary,
    primaryContainer    = KeKitaPrimaryLight.copy(alpha = 0.12f),
    onPrimaryContainer  = KeKitaPrimaryDark,
    background          = KeKitaBackground,
    onBackground        = KeKitaTextPrimary,
    surface             = KeKitaSurface,
    onSurface           = KeKitaTextPrimary,
    onSurfaceVariant    = KeKitaTextSecondary,
    outline             = KeKitaDivider,
    outlineVariant      = KeKitaDivider,
    tertiary            = KeKitaSuccess,
    tertiaryContainer   = KeKitaSuccessContainer,
    onTertiaryContainer = KeKitaSuccess,
    error               = KeKitaError,
    onError             = Color.White,
    errorContainer      = KeKitaErrorContainer,
    onErrorContainer    = KeKitaError,
)

// ─── Dark Color Scheme ────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = KeKitaDarkPrimary,
    onPrimary           = KeKitaDarkOnPrimary,
    primaryContainer    = KeKitaDarkPrimaryDark.copy(alpha = 0.25f),
    onPrimaryContainer  = KeKitaDarkPrimaryLight,
    background          = KeKitaDarkBackground,
    onBackground        = KeKitaDarkTextPrimary,
    surface             = KeKitaDarkSurface,
    onSurface           = KeKitaDarkTextPrimary,
    onSurfaceVariant    = KeKitaDarkTextSecondary,
    outline             = KeKitaDarkDivider,
    outlineVariant      = KeKitaDarkDivider,
    tertiary            = KeKitaDarkSuccess,
    tertiaryContainer   = KeKitaDarkSuccessContainer,
    onTertiaryContainer = KeKitaDarkSuccess,
    error               = KeKitaDarkError,
    onError             = Color(0xFF450A0A),
    errorContainer      = KeKitaDarkErrorContainer,
    onErrorContainer    = KeKitaDarkOnErrorContainer,
)

@Composable
fun POSBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // dimatikan agar warna brand KeKita selalu konsisten
    content: @Composable () -> Unit
) {
    // dynamicColor sengaja diabaikan
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
