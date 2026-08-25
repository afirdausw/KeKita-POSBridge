package devyana.kekita.posbridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.ui.accesscode.AccessCodeScreen
import devyana.kekita.posbridge.ui.accesscode.AccessCodeViewModel
import devyana.kekita.posbridge.ui.accesscode.AccessCodeViewModelFactory
import devyana.kekita.posbridge.ui.dashboard.MainDashboardScreen
import devyana.kekita.posbridge.ui.home.HomeViewModel
import devyana.kekita.posbridge.ui.home.HomeViewModelFactory
import devyana.kekita.posbridge.ui.login.LoginScreen
import devyana.kekita.posbridge.ui.login.LoginViewModel
import devyana.kekita.posbridge.ui.login.LoginViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    outletRepository: OutletRepository,
    authRepository: AuthRepository,
    productRepository: devyana.kekita.posbridge.data.repository.ProductRepository
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ─── Step 1: Kode Akses Outlet ────────────────────────────────────────
        composable(Screen.AccessCode.route) {
            val factory = AccessCodeViewModelFactory(outletRepository)
            val viewModel: AccessCodeViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsState()

            AccessCodeScreen(
                uiState = uiState,
                onVerifyClick = { code -> viewModel.verifyCode(code) },
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AccessCode.route) { inclusive = true }
                    }
                },
                onErrorShown = { viewModel.resetState() }
            )
        }

        // ─── Step 2: Login Kasir / Waiter ─────────────────────────────────────
        composable(Screen.Login.route) {
            val factory = LoginViewModelFactory(authRepository)
            val viewModel: LoginViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsState()

            LoginScreen(
                uiState = uiState,
                onLoginClick = { username, password -> viewModel.login(username, password) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onErrorShown = { viewModel.resetState() }
            )
        }

        // ─── Main Dashboard POS (Static Sidebar Shell + Dynamic Content) ──────
        composable(Screen.Home.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val posPreferenceManager = remember { devyana.kekita.posbridge.utils.PosPreferenceManager(context) }
            val factory = HomeViewModelFactory(authRepository, outletRepository, productRepository, posPreferenceManager)
            val viewModel: HomeViewModel = viewModel(factory = factory)

            MainDashboardScreen(
                homeViewModel = viewModel,
                onLogoutAccount = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onLogoutSystem = {
                    navController.navigate(Screen.AccessCode.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
