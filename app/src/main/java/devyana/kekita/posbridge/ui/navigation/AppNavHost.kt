package devyana.kekita.posbridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.ui.accesscode.AccessCodeScreen
import devyana.kekita.posbridge.ui.accesscode.AccessCodeViewModel
import devyana.kekita.posbridge.ui.accesscode.AccessCodeViewModelFactory
import devyana.kekita.posbridge.ui.home.HomeScreen
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
    authRepository: AuthRepository
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

        // ─── Home ─────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            val factory = HomeViewModelFactory(authRepository, outletRepository)
            val viewModel: HomeViewModel = viewModel(factory = factory)

            HomeScreen(
                viewModel = viewModel,
                onLogoutAccount = {
                    // Logout akun → kembali ke Login, outlet masih ada
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onLogoutSystem = {
                    // Logout sistem → kembali ke AccessCode, semua direset
                    navController.navigate(Screen.AccessCode.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
