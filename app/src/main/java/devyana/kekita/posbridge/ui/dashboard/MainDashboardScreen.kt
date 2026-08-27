package devyana.kekita.posbridge.ui.dashboard

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import devyana.kekita.posbridge.ui.checker.CheckerScreenContent
import devyana.kekita.posbridge.ui.components.LogoutDialog
import devyana.kekita.posbridge.ui.components.PosSidebar
import devyana.kekita.posbridge.ui.home.HomeScreenContent
import devyana.kekita.posbridge.ui.home.HomeViewModel
import devyana.kekita.posbridge.ui.navigation.Screen
import devyana.kekita.posbridge.ui.payment.PaymentScreenContent
import devyana.kekita.posbridge.ui.product.ProductScreenContent
import devyana.kekita.posbridge.ui.report.ReportScreenContent
import devyana.kekita.posbridge.ui.theme.PosContentBg
import devyana.kekita.posbridge.ui.theme.PosSidebarBg
import devyana.kekita.posbridge.ui.transaction.TransactionScreenContent

private val menuRouteOrder = listOf(
    Screen.Home.route,
    Screen.Transaction.route,
    Screen.Product.route,
    Screen.Report.route,
    Screen.Payment.route,
        Screen.Checker.route,
    Screen.Settings.route
)

@Composable
fun MainDashboardScreen(
    homeViewModel: HomeViewModel,
    onLogoutAccount: () -> Unit,
    onLogoutSystem: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var showLogoutAccountDialog by remember { mutableStateOf(false) }
    var showLogoutSystemDialog by remember { mutableStateOf(false) }
    var backPressedTime by remember { mutableLongStateOf(0L) }

    // Handing Double Back Press & Tab Navigation
    BackHandler {
        if (currentRoute != Screen.Home.route) {
            // Kembali ke halaman utama POS jika sedang di tab lain
            currentRoute = Screen.Home.route
        } else {
            // Jika sudah di POS Utama, minta konfirmasi klik back sekali lagi untuk keluar
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                activity?.finish()
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, "Tekan sekali lagi untuk keluar dari aplikasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showLogoutAccountDialog) {
        LogoutDialog(
            title = "Logout Akun",
            message = "Yakin ingin keluar dari akun ini? Outlet tetap terkonfigurasi.",
            confirmText = "Ya, Logout",
            onDismiss = { showLogoutAccountDialog = false },
            onConfirm = {
                showLogoutAccountDialog = false
                homeViewModel.logoutAccount()
                onLogoutAccount()
            }
        )
    }

    if (showLogoutSystemDialog) {
        LogoutDialog(
            title = "Logout Sistem",
            message = "Ini akan menghapus akun dan konfigurasi outlet. Aplikasi kembali ke awal.",
            confirmText = "Ya, Reset",
            onDismiss = { showLogoutSystemDialog = false },
            onConfirm = {
                showLogoutSystemDialog = false
                homeViewModel.logoutSystem()
                onLogoutSystem()
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PosSidebarBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Sidebar tetap stay di sebelah kiri (Dark theme token)
        val uiState by homeViewModel.uiState.collectAsState()
        PosSidebar(
            currentRoute = currentRoute,
            logoUrl = uiState.homeData?.logoUrl ?: "",
            syncState = uiState.syncState,
            onSyncClick = { homeViewModel.pingServer() },
            onNavigateToRoute = { newRoute -> currentRoute = newRoute },
            onLogoutAccount = { showLogoutAccountDialog = true },
            onLogoutSystem = onLogoutSystem
        )

        // Floating Content Card (Overlapping Sidebar dengan rounded topStart & bottomStart 24.dp)
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            color = PosContentBg
        ) {
            // Animasi Directional Vertical Slide ringan & responsif tanpa lag
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    val initialIndex = menuRouteOrder.indexOf(initialState).let { if (it == -1) 0 else it }
                    val targetIndex = menuRouteOrder.indexOf(targetState).let { if (it == -1) 0 else it }

                    if (targetIndex > initialIndex) {
                        // Pindah ke menu bawah -> Slide masuk dari bawah ke atas
                        (slideInVertically(
                            animationSpec = tween(180),
                            initialOffsetY = { fullHeight -> fullHeight / 5 }
                        ) + fadeIn(tween(150))) togetherWith (
                            slideOutVertically(
                                animationSpec = tween(180),
                                targetOffsetY = { fullHeight -> -fullHeight / 5 }
                            ) + fadeOut(tween(150))
                        )
                    } else {
                        // Pindah ke menu atas -> Slide masuk dari atas ke bawah
                        (slideInVertically(
                            animationSpec = tween(180),
                            initialOffsetY = { fullHeight -> -fullHeight / 5 }
                        ) + fadeIn(tween(150))) togetherWith (
                            slideOutVertically(
                                animationSpec = tween(180),
                                targetOffsetY = { fullHeight -> fullHeight / 5 }
                            ) + fadeOut(tween(150))
                        )
                    }
                },
                label = "DirectionalTabSlide"
            ) { targetRoute ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (targetRoute) {
                        Screen.Home.route -> HomeScreenContent(
                            viewModel = homeViewModel,
                            onNavigateToPayment = { currentRoute = Screen.Payment.route }
                        )
                        Screen.Transaction.route -> TransactionScreenContent(
                            transactions = homeViewModel.transactions.collectAsState().value,
                            onProcessPayment = { txId, paidAmount ->
                                homeViewModel.updateTransactionToPaid(txId, paidAmount)
                            }
                        )
                        Screen.Product.route -> ProductScreenContent()
                        Screen.Report.route -> ReportScreenContent()
                        Screen.Payment.route -> PaymentScreenContent(
//                            transactions = homeViewModel.transactions.collectAsState().value,
                            isRefreshing = uiState.syncState == devyana.kekita.posbridge.ui.home.ServerSyncState.SYNCING_DOWN,
                            onRefresh = { homeViewModel.simulateSync() },
                            onNavigateToPos = { currentRoute = Screen.Home.route }
                        )
                        Screen.Checker.route -> CheckerScreenContent(
//                            transactions = homeViewModel.transactions.collectAsState().value,
                            isRefreshing = uiState.syncState == devyana.kekita.posbridge.ui.home.ServerSyncState.SYNCING_DOWN,
                            onRefresh = { homeViewModel.simulateSync() },
                            onNavigateToPos = { currentRoute = Screen.Home.route }
                        )
                        Screen.Settings.route -> devyana.kekita.posbridge.ui.settings.SettingsScreenContent(
                            viewModel = homeViewModel,
                            onLogoutAccount = { showLogoutAccountDialog = true },
                            onLogoutSystem = { showLogoutSystemDialog = true }
                        )
                        else -> HomeScreenContent(viewModel = homeViewModel)
                    }
                }
            }
        }
    }
}
