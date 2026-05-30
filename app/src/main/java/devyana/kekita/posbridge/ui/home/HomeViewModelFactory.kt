package devyana.kekita.posbridge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val outletRepository: OutletRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(authRepository, outletRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
