package devyana.kekita.posbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import devyana.kekita.posbridge.data.local.database.AppDatabase
import devyana.kekita.posbridge.data.remote.network.RetrofitClient
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.ui.navigation.AppNavHost
import devyana.kekita.posbridge.ui.navigation.Screen
import devyana.kekita.posbridge.ui.theme.POSBridgeTheme
import devyana.kekita.posbridge.utils.OutletManager
import devyana.kekita.posbridge.utils.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ─── Inisialisasi dependencies ────────────────────────────────────────
        val outletManager = OutletManager(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        val outletRepository = OutletRepository(
            outletApiService = RetrofitClient.outletApiService,
            outletManager = outletManager
        )
        val authRepository = AuthRepository(
            outletManager = outletManager,
            sessionManager = sessionManager
        )

        val productRepository = devyana.kekita.posbridge.data.repository.ProductRepository(
            productDao = AppDatabase.getInstance(applicationContext).productDao(),
            outletManager = outletManager
        )

        val transactionRepository = devyana.kekita.posbridge.data.repository.TransactionRepository(
            transactionDao = AppDatabase.getInstance(applicationContext).transactionDao()
        )

        // ─── Tentukan start destination ───────────────────────────────────────
        // Jika outlet belum dikonfigurasi → AccessCode (Step 1)
        // Jika sudah dikonfigurasi tapi belum login → Login (Step 2)
        // Jika sudah login → langsung Home
        val startDestination = when {
            !outletManager.isOutletConfigured() -> Screen.AccessCode.route
            !sessionManager.isLoggedIn() -> Screen.Login.route
            else -> Screen.Home.route
        }

        setContent {
            POSBridgeTheme(dynamicColor = false) {
                val navController = rememberNavController()

                AppNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    outletRepository = outletRepository,
                    authRepository = authRepository,
                    productRepository = productRepository,
                    transactionRepository = transactionRepository
                )
            }
        }
    }
}