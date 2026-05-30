package devyana.kekita.posbridge.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogoutAccount: () -> Unit,
    onLogoutSystem: () -> Unit
) {
    val data by viewModel.homeData.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var showLogoutAccountDialog by remember { mutableStateOf(false) }
    var showLogoutSystemDialog  by remember { mutableStateOf(false) }

    // ─── Dialog Konfirmasi Logout Akun ────────────────────────────────────────
    if (showLogoutAccountDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutAccountDialog = false },
            title = { Text("Logout Akun") },
            text  = { Text("Yakin ingin keluar dari akun ini?\nOutlet tetap terkonfigurasi.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutAccountDialog = false
                    viewModel.logoutAccount()
                    onLogoutAccount()
                }) { Text("Ya, Logout", color = colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutAccountDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // ─── Dialog Konfirmasi Logout Sistem ──────────────────────────────────────
    if (showLogoutSystemDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutSystemDialog = false },
            title = { Text("Logout Sistem") },
            text  = { Text("Ini akan menghapus akun dan konfigurasi outlet.\nAplikasi kembali ke awal.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutSystemDialog = false
                    viewModel.logoutSystem()
                    onLogoutSystem()
                }) { Text("Ya, Reset", color = colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutSystemDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ─── Header ───────────────────────────────────────────────────────────
        Text(
            text = "Informasi Sesi",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground
            )
        )

        // ─── Card: Info User ──────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Avatar + nama
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = data?.displayName ?: "-",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "@${data?.username ?: "-"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                InfoRow(
                    icon  = Icons.Default.PhoneAndroid,
                    label = "Level / Peran",
                    value = data?.role ?: "-"
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Konfigurasi Outlet",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground
            )
        )

        // ─── Card: Info Outlet ────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                InfoRow(
                    icon  = Icons.Default.Business,
                    label = "Nama Outlet",
                    value = data?.outletName ?: "-"
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(
                    icon  = Icons.Default.Language,
                    label = "API Domain",
                    value = data?.apiDomain ?: "-",
                    valueMaxLines = 1
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(
                    icon  = Icons.Default.Receipt,
                    label = "Header Struk",
                    value = data?.headerText ?: "-"
                )

                if (!data?.footerText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(
                        icon  = Icons.Default.Receipt,
                        label = "Footer Struk",
                        value = data?.footerText ?: "-"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(8.dp))

        // ─── Tombol Logout ────────────────────────────────────────────────────
        Button(
            onClick  = { showLogoutAccountDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.error,
                contentColor   = colorScheme.onError
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout Akun",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick  = { showLogoutSystemDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorScheme.error
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout Sistem",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "v1.0 · KeKita POS Bridge",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// ─── Komponen baris informasi ──────────────────────────────────────────────────
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueMaxLines: Int = Int.MAX_VALUE
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colorScheme.onSurfaceVariant
                )
            )
            Text(
                text     = value,
                style    = MaterialTheme.typography.bodyMedium.copy(
                    color      = colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = valueMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
