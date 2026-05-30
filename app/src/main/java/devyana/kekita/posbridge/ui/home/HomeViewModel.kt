package devyana.kekita.posbridge.ui.home

import androidx.lifecycle.ViewModel
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.utils.OutletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeData(
    // ─── User Session ──────────────────────────────────────────────
    val displayName: String,
    val username: String,
    val role: String,
    // ─── Outlet Config ─────────────────────────────────────────────
    val outletName: String,
    val apiDomain: String,
    val headerText: String,
    val footerText: String,
    val logoUrl: String
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _homeData = MutableStateFlow<HomeData?>(null)
    val homeData: StateFlow<HomeData?> = _homeData.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val outlet: OutletManager.OutletConfig? = outletRepository.getOutletConfig()
        _homeData.value = HomeData(
            displayName = authRepository.getDisplayName() ?: "-",
            username    = authRepository.getUsername()    ?: "-",
            role        = authRepository.getRole()        ?: "-",
            outletName  = outlet?.name       ?: "-",
            apiDomain   = outlet?.apiDomain  ?: "-",
            headerText  = outlet?.headerText ?: "-",
            footerText  = outlet?.footerText ?: "-",
            logoUrl     = outlet?.logo       ?: ""
        )
    }

    /** Logout akun saja — outlet config tetap, kembali ke LoginScreen */
    fun logoutAccount() {
        authRepository.clearSession()
    }

    /** Logout sistem — hapus akun + outlet config, kembali ke AccessCodeScreen */
    fun logoutSystem() {
        authRepository.clearSession()
        outletRepository.clearOutletConfig()
    }
}
