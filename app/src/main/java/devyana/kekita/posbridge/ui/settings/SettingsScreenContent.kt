package devyana.kekita.posbridge.ui.settings

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import devyana.kekita.posbridge.ui.home.HomeViewModel
import devyana.kekita.posbridge.ui.theme.KeKitaDarkTextPrimary
import devyana.kekita.posbridge.ui.theme.PosContentBg
import devyana.kekita.posbridge.utils.Constants

@Composable
fun SettingsScreenContent(
    viewModel: HomeViewModel,
    onLogoutAccount: () -> Unit,
    onLogoutSystem: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.homeData
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    // Ambil semua data SharedPreferences
    val allPrefs = remember {
        val prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        prefs.all
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PosContentBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        )

        // ─── Card 1: Info Akun (Kasir) ──────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = data?.displayName ?: "-",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "@${data?.username ?: "-"} • ${data?.role ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // ─── Card 2: Semua Data SharedPreferences (Debug/Info) ───────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Informasi Session & Outlet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                val sortedPrefs = allPrefs.entries.sortedBy { it.key }
                sortedPrefs.forEachIndexed { index, entry ->
                    val displayValue = if (entry.value is String) {
                        (entry.value as String).replace("\n", ", ")
                    } else {
                        entry.value.toString()
                    }
                    InfoRowBetween(entry.key, displayValue)
                    if (index < sortedPrefs.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = PosContentBg
                        )
                    }
                }
            }
        }

        // ─── Menu List ────────────────────────────────────────────────────────
        Text(
            text = "Menu Pengaturan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column {
                SettingsMenuItem(
                    icon = Icons.Default.Print,
                    title = "Printer",
                    subtitle = "Pengaturan cetak struk Bluetooth",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = KeKitaDarkTextPrimary)
                
                SettingsMenuItem(
                    icon = Icons.Default.Store,
                    title = "Outlet",
                    subtitle = "Kelola detail outlet",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = KeKitaDarkTextPrimary)
                
                SettingsMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Bantuan",
                    subtitle = "Pusat bantuan dan panduan",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = KeKitaDarkTextPrimary)
                
                SettingsMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    subtitle = "Keluar dari akun kasir ini",
                    iconTint = colorScheme.error,
                    titleColor = colorScheme.error,
                    onClick = onLogoutAccount
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Ingin reset keseluruhan aplikasi?",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Logout Sistem",
            style = MaterialTheme.typography.titleMedium.copy(
                color = colorScheme.error,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onLogoutSystem() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun InfoRowBetween(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f, fill = false).padding(start = 16.dp)
        )
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colorScheme.onSurfaceVariant
                )
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}
