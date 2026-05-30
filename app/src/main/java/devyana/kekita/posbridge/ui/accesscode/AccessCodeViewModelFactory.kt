package devyana.kekita.posbridge.ui.accesscode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devyana.kekita.posbridge.data.repository.OutletRepository

class AccessCodeViewModelFactory(
    private val outletRepository: OutletRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccessCodeViewModel::class.java)) {
            return AccessCodeViewModel(outletRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
