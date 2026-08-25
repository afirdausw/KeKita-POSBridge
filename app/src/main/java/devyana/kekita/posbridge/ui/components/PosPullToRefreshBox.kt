package devyana.kekita.posbridge.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    
    // Animasi transisi warna ala Google Sweep (Biru -> Merah -> Kuning -> Hijau)
    val infiniteTransition = rememberInfiniteTransition(label = "ptrColor")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF4285F4),
        targetValue = Color(0xFF4285F4),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                Color(0xFF4285F4) at 0 // Blue
                Color(0xFFEA4335) at 500 // Red
                Color(0xFFFBBC05) at 1000 // Yellow
                Color(0xFF34A853) at 1500 // Green
                Color(0xFF4285F4) at 2000 // Blue
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "colorKeyframes"
    )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = state,
                color = animatedColor
            )
        },
        content = content
    )
}
