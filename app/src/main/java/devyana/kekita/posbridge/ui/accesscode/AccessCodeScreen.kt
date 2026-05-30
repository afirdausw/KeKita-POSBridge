package devyana.kekita.posbridge.ui.accesscode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.theme.POSBridgeTheme

@Composable
fun AccessCodeScreen(
    uiState: AccessCodeUiState,
    onVerifyClick: (code: String) -> Unit,
    onSuccess: () -> Unit,
    onErrorShown: () -> Unit
) {
    var accessCode by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading = uiState is AccessCodeUiState.Loading

    // Warna diambil dari MaterialTheme — otomatis ikut light/dark
    val colorScheme = MaterialTheme.colorScheme
    val shadowElevation = if (isSystemInDarkTheme()) 0.dp else 6.dp

    LaunchedEffect(uiState) {
        if (uiState is AccessCodeUiState.Success) onSuccess()
    }

    LaunchedEffect(uiState) {
        if (uiState is AccessCodeUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
            onErrorShown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ─── Snackbar ─────────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = colorScheme.errorContainer,
                contentColor = colorScheme.onErrorContainer,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // ─── Content ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ─── Logo + Judul ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(
                                elevation = shadowElevation,
                                shape = RoundedCornerShape(20.dp),
                                clip = false
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_icon),
                            contentDescription = "KeKita Logo"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // "KeKita" highlight, " POS Bridge" warna onBackground
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) { append("KeKita") }
                            withStyle(
                                SpanStyle(
                                    color = colorScheme.onBackground,
                                    fontWeight = FontWeight.Medium
                                )
                            ) { append(" POS Bridge") }
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Manajemen Outlet & Kasir",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ─── Step Badge ───────────────────────────────────────────────────
            Text(
                text = "LANGKAH 1 DARI 2",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Card ─────────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Konfigurasi Outlet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Masukkan kode akses yang diberikan\noleh administrator untuk outlet ini.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ─── Input Kode Akses ─────────────────────────────────────
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it.uppercase() },
                        label = { Text(text = "Kode Akses Outlet") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Kode Akses"
                            )
                        },
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (!isLoading) onVerifyClick(accessCode)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant,
                            focusedLeadingIconColor = colorScheme.primary,
                            unfocusedLeadingIconColor = colorScheme.onSurfaceVariant,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary,
                            disabledBorderColor = colorScheme.outline,
                            disabledTextColor = colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Tombol Verifikasi ────────────────────────────────────
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onVerifyClick(accessCode)
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                            disabledContainerColor = colorScheme.primary.copy(alpha = 0.38f),
                            disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                    ) {
                        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        }
                        AnimatedVisibility(visible = !isLoading, enter = fadeIn(), exit = fadeOut()) {
                            Text(
                                text = "Verifikasi Kode",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Hubungi administrator jika belum memiliki kode akses.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun AccessCodePreviewLight() {
    POSBridgeTheme(darkTheme = false) {
        AccessCodeScreen(
            uiState = AccessCodeUiState.Idle,
            onVerifyClick = { },
            onSuccess = {},
            onErrorShown = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
fun AccessCodePreviewDark() {
    POSBridgeTheme(darkTheme = true) {
        AccessCodeScreen(
            uiState = AccessCodeUiState.Idle,
            onVerifyClick = { },
            onSuccess = {},
            onErrorShown = {}
        )
    }
}