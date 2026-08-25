package devyana.kekita.posbridge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.utils.PosPreferenceManager

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val outletRepository: OutletRepository,
    private val productRepository: devyana.kekita.posbridge.data.repository.ProductRepository,
    private val posPreferenceManager: PosPreferenceManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(authRepository, outletRepository, productRepository, posPreferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
