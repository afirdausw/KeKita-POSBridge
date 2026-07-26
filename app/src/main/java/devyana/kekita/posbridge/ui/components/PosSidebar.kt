package devyana.kekita.posbridge.ui.components

import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.navigation.Screen
import devyana.kekita.posbridge.ui.theme.PosInactiveIcon
import devyana.kekita.posbridge.ui.theme.PosLimeActive
import devyana.kekita.posbridge.ui.theme.PosLimeActiveIcon
import devyana.kekita.posbridge.ui.theme.PosSidebarBg
import devyana.kekita.posbridge.ui.theme.PosSidebarDivider

@Composable
fun PosSidebar(
    currentRoute: String,
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
            ComposeImage(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
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

        // Line Divider Spasi Bottom
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(PosSidebarDivider)
        )
        Spacer(modifier = Modifier.height(14.dp))

        // ─── Kelompok 3: Logout Akun (Lucide Icon) ───────────────────────────
        SidebarLucideItem(
            iconRes = R.drawable.ic_lucide_log_out,
            selected = false,
            activeColor = PosLimeActive,
            activeIconColor = PosLimeActiveIcon,
            inactiveIconColor = Color(0xFFEF4444),
            contentDescription = "Logout Akun",
            onClick = onLogoutAccount
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
