package devyana.kekita.posbridge.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.navigation.Screen
import devyana.kekita.posbridge.ui.home.ServerSyncState
import devyana.kekita.posbridge.ui.theme.PosInactiveIcon
import devyana.kekita.posbridge.ui.theme.PosLimeActive
import devyana.kekita.posbridge.ui.theme.PosLimeActiveIcon
import devyana.kekita.posbridge.ui.theme.PosSidebarBg
import devyana.kekita.posbridge.ui.theme.PosSidebarDivider
import kotlinx.coroutines.delay

enum class LiveSyncState(
    val label: String,
    val iconRes: Int,
    val color: Color,
    val durationMs: Long,
    val isRotating: Boolean = false,
    val isPulsing: Boolean = false
) {
    CONNECTING("connection", R.drawable.ic_lucide_wifi, Color(0xFFF59E0B), 3000L, isPulsing = true),
    SYNCING_1("sync down", R.drawable.ic_lucide_refresh_cw, Color(0xFF06B6D4), 3000L, isRotating = true),
    CONNECTED("connected", R.drawable.ic_lucide_cloud_check, Color(0xFF10B981), 5000L),
    IDLE("idle", R.drawable.ic_lucide_server, Color(0xFF94A3B8), 3000L),
    SYNCING_2("sync up", R.drawable.ic_lucide_cloud_upload, Color(0xFF06B6D4), 3000L, isPulsing = true),
    DISCONNECTED("disconnect", R.drawable.ic_lucide_wifi_off, Color(0xFFEF4444), 5000L, isPulsing = true)
}

@Composable
fun PosSidebar(
    currentRoute: String,
    logoUrl: String,
    syncState: ServerSyncState,
    onSyncClick: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onLogoutAccount: () -> Unit,
    onLogoutSystem: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .background(PosSidebarBg)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo / Icon Outlet di Atas
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PosSidebarDivider),
            contentAlignment = Alignment.Center
        ) {
            if (logoUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Outlet Logo",
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    fallback = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            } else {
                ComposeImage(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Default Logo",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Kelompok 1: POS, Transaksi, Produk, Laporan (Lucide Icons) ────────
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_layout_grid,
            selected = currentRoute == Screen.Home.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "POS Utama",
            onClick = { onNavigateToRoute(Screen.Home.route) }
        )
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_history,
            selected = currentRoute == Screen.Transaction.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "Daftar Transaksi",
            onClick = { onNavigateToRoute(Screen.Transaction.route) }
        )
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_package,
            selected = currentRoute == Screen.Product.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "Daftar Produk",
            onClick = { onNavigateToRoute(Screen.Product.route) }
        )
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_bar_chart,
            selected = currentRoute == Screen.Report.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "Laporan",
            onClick = { onNavigateToRoute(Screen.Report.route) }
        )

        // Line Divider Spasi
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(PosSidebarDivider)
        )
        Spacer(modifier = Modifier.height(14.dp))

        // ─── Kelompok 2: Pembayaran & Checker (Lucide Icons) ─────────────────
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_credit_card,
            selected = currentRoute == Screen.Payment.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "Pembayaran",
            onClick = { onNavigateToRoute(Screen.Payment.route) }
        )
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_utensils,
            selected = currentRoute == Screen.Checker.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "Order Checker",
            onClick = { onNavigateToRoute(Screen.Checker.route) }
        )

        // Flexible Space to Bottom
        Spacer(modifier = Modifier.weight(1f))

        // ─── LIVE SERVER SYNC STATUS INDICATOR (SIMULASI SERVER SYNC) ──────────
        ServerSyncStatusWidget(
            syncState = syncState,
            onClick = onSyncClick
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Line Divider Spasi Bottom
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(PosSidebarDivider)
        )
        Spacer(modifier = Modifier.height(14.dp))

        // ─── Kelompok 3: Pengaturan (Lucide Icon) ───────────────────────────
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_settings,
            selected = currentRoute == Screen.Settings.route,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = PosInactiveIcon,
            contentDescription = "Pengaturan",
            onClick = { onNavigateToRoute(Screen.Settings.route) }
        )
    }
}

@Composable
private fun ServerSyncStatusWidget(
    syncState: ServerSyncState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentState = remember(syncState) {
        when (syncState) {
            ServerSyncState.IDLE -> LiveSyncState.CONNECTED
            ServerSyncState.SYNCING_DOWN -> LiveSyncState.SYNCING_1
            ServerSyncState.SYNCING_UP -> LiveSyncState.SYNCING_2
            ServerSyncState.PINGING -> LiveSyncState.SYNCING_1
            ServerSyncState.ERROR_OFFLINE -> LiveSyncState.DISCONNECTED
        }
    }

    // Continuous rotation transition for Syncing state
    val infiniteTransition = rememberInfiniteTransition(label = "SyncAnimation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse opacity transition for Connecting / Disconnected states
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val currentAlpha = if (currentState.isPulsing) pulseAlpha else 0.85f

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner Circle Backdrop
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(currentState.color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = currentState.iconRes),
                contentDescription = currentState.label,
                tint = currentState.color.copy(alpha = currentAlpha),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        if (currentState.isRotating) {
                            rotationZ = rotationAngle
                        }
                    }
            )
        }

        // Subtle status dot at top right (Unclipped, with subtle border ring)
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(PosSidebarBg, CircleShape)
                .padding(1.dp)
                .background(currentState.color.copy(alpha = currentAlpha), CircleShape)
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun SidebarLucideItem(
    iconRes: Int,
    selected: Boolean,
    activeColor: Color,
    activeIconColor: Color,
    inactiveIconColor: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeColor else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = if (selected) activeIconColor else inactiveIconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
