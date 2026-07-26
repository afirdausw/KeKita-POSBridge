package devyana.kekita.posbridge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.utils.OutletManager
import devyana.kekita.posbridge.utils.PosPreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DUMMY_PRODUCT_TOTAL = 211
private const val FOOD_CATEGORY = "Makanan"
private const val DRINK_CATEGORY = "Minuman"

data class HomeData(
    val displayName: String,
    val username: String,
    val role: String,
    val outletName: String,
    val apiDomain: String,
    val headerText: String,
    val footerText: String,
    val logoUrl: String
)

data class PosProduct(
    val id: Long,
    val name: String,
    val category: String,
    val price: Int,
    val isAvailable: Boolean = true,
    val isSelected: Boolean = false,
    val hasAddon: Boolean = false,
    val variants: List<String> = emptyList(),
    val warningLevel: Int = 0
)

data class CartItem(
    val id: String,
    val product: PosProduct,
    val selectedVariant: String? = null,
    val quantity: Int = 1,
    val note: String? = null
) {
    val displayName: String
        get() = product.name

    val subtotal: Int = product.price * quantity
}

data class TableItem(
    val id: String,
    val name: String,
    val status: String = "Tersedia"
)

data class HomePosUiState(
    val homeData: HomeData? = null,
    val invoiceNumber: String = "Invoice #260726001",
    val businessDate: String = "",
    val selectedCategory: String = "Semua",
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val products: List<PosProduct> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val totalProductCount: Int = DUMMY_PRODUCT_TOTAL,
    // Variant dialog state
    val selectedProductForVariant: PosProduct? = null,
    // Note dialog state
    val selectedCartItemForNote: CartItem? = null,
    val noteInputText: String = "",
    // Table offcanvas state
    val showTableOffcanvas: Boolean = false,
    val tables: List<TableItem> = emptyList(),
    val tempSelectedTable: TableItem? = null,
    val confirmedTable: TableItem? = null
) {
    val filteredProducts: List<PosProduct>
        get() = products.filter { product ->
            val matchesCategory = selectedCategory == "Semua" || product.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

    val foodCount: Int
        get() = cartItems
            .filter { it.product.category == FOOD_CATEGORY }
            .sumOf { it.quantity }

    val drinkCount: Int
        get() = cartItems
            .filter { it.product.category == DRINK_CATEGORY }
            .sumOf { it.quantity }

    val subtotal: Int
        get() = cartItems.sumOf { it.subtotal }

    val totalCartItemCount: Int
        get() = cartItems.sumOf { it.quantity }
}

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val outletRepository: OutletRepository,
    private val posPreferenceManager: PosPreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePosUiState())
    val uiState: StateFlow<HomePosUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startClock()
    }

    private fun startClock() {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy | HH:mm:ss", Locale("id", "ID"))
            while (isActive) {
                val currentTimeFormatted = dateFormat.format(Date())
                _uiState.update { it.copy(businessDate = currentTimeFormatted) }
                delay(1000)
            }
        }
    }

    private fun loadData() {
        val outlet: OutletManager.OutletConfig? = outletRepository.getOutletConfig()
        val homeData = HomeData(
            displayName = authRepository.getDisplayName() ?: "-",
            username = authRepository.getUsername() ?: "-",
            role = authRepository.getRole() ?: "-",
            outletName = outlet?.name ?: "KeKita",
            apiDomain = outlet?.apiDomain ?: "-",
            headerText = outlet?.headerText ?: "-",
            footerText = outlet?.footerText ?: "-",
            logoUrl = outlet?.logo ?: ""
        )

        // Restore persisted cart & confirmed table from SharedPreferences
        val savedCart = posPreferenceManager.getCartItems()
        val savedTable = posPreferenceManager.getConfirmedTable()
        val activeProductIds = savedCart.map { it.product.id }.toSet()

        val initialProducts = dummyProducts.map { product ->
            product.copy(isSelected = activeProductIds.contains(product.id))
        }

        _uiState.value = HomePosUiState(
            homeData = homeData,
            categories = dummyCategories,
            products = initialProducts,
            tables = dummyTables,
            cartItems = savedCart,
            confirmedTable = savedTable
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onProductClick(product: PosProduct) {
        if (product.variants.isNotEmpty()) {
            _uiState.update { it.copy(selectedProductForVariant = product) }
        } else {
            addProductToCart(product, variant = null)
        }
    }

    fun selectVariantAndAdd(product: PosProduct, variant: String) {
        addProductToCart(product, variant)
        _uiState.update { it.copy(selectedProductForVariant = null) }
    }

    fun dismissVariantDialog() {
        _uiState.update { it.copy(selectedProductForVariant = null) }
    }

    private fun addProductToCart(product: PosProduct, variant: String?) {
        val itemId = "${product.id}_${variant ?: ""}"
        _uiState.update { state ->
            val currentItems = state.cartItems.toMutableList()
            val existingIndex = currentItems.indexOfFirst { it.id == itemId }

            if (existingIndex >= 0) {
                val existing = currentItems[existingIndex]
                currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
            } else {
                currentItems.add(
                    CartItem(
                        id = itemId,
                        product = product,
                        selectedVariant = variant,
                        quantity = 1
                    )
                )
            }

            posPreferenceManager.saveCartItems(currentItems)
            val activeProductIds = currentItems.map { it.product.id }.toSet()

            state.copy(
                products = state.products.map { item ->
                    item.copy(isSelected = activeProductIds.contains(item.id))
                },
                cartItems = currentItems
            )
        }
    }

    fun incrementCartItem(itemId: String) {
        _uiState.update { state ->
            val updated = state.cartItems.map { item ->
                if (item.id == itemId) item.copy(quantity = item.quantity + 1) else item
            }
            posPreferenceManager.saveCartItems(updated)
            state.copy(cartItems = updated)
        }
    }

    fun decrementCartItem(itemId: String) {
        _uiState.update { state ->
            val target = state.cartItems.firstOrNull { it.id == itemId } ?: return@update state
            val updatedItems = if (target.quantity > 1) {
                state.cartItems.map { item ->
                    if (item.id == itemId) item.copy(quantity = item.quantity - 1) else item
                }
            } else {
                state.cartItems.filterNot { it.id == itemId }
            }

            posPreferenceManager.saveCartItems(updatedItems)
            val activeProductIds = updatedItems.map { it.product.id }.toSet()

            state.copy(
                cartItems = updatedItems,
                products = state.products.map { item ->
                    item.copy(isSelected = activeProductIds.contains(item.id))
                }
            )
        }
    }

    fun removeCartItem(itemId: String) {
        _uiState.update { state ->
            val updatedItems = state.cartItems.filterNot { it.id == itemId }
            posPreferenceManager.saveCartItems(updatedItems)
            val activeProductIds = updatedItems.map { it.product.id }.toSet()

            state.copy(
                cartItems = updatedItems,
                products = state.products.map { item ->
                    item.copy(isSelected = activeProductIds.contains(item.id))
                }
            )
        }
    }

    fun clearCart() {
        posPreferenceManager.saveCartItems(emptyList())
        posPreferenceManager.saveConfirmedTable(null)
        _uiState.update { state ->
            state.copy(
                products = state.products.map { it.copy(isSelected = false) },
                cartItems = emptyList(),
                confirmedTable = null,
                tempSelectedTable = null
            )
        }
    }

    // Note Dialog handlers
    fun openNoteDialog(cartItem: CartItem) {
        _uiState.update { state ->
            state.copy(
                selectedCartItemForNote = cartItem,
                noteInputText = cartItem.note ?: ""
            )
        }
    }

    fun updateNoteInputText(text: String) {
        _uiState.update { it.copy(noteInputText = text) }
    }

    fun dismissNoteDialog() {
        _uiState.update { it.copy(selectedCartItemForNote = null, noteInputText = "") }
    }

    fun saveNote() {
        _uiState.update { state ->
            val itemToUpdate = state.selectedCartItemForNote ?: return@update state
            val updatedList = state.cartItems.map { item ->
                if (item.id == itemToUpdate.id) {
                    item.copy(note = state.noteInputText.ifBlank { null })
                } else item
            }
            posPreferenceManager.saveCartItems(updatedList)
            state.copy(
                cartItems = updatedList,
                selectedCartItemForNote = null,
                noteInputText = ""
            )
        }
    }

    // Table Offcanvas handlers
    fun openTableOffcanvas() {
        _uiState.update { state ->
            state.copy(
                showTableOffcanvas = true,
                tempSelectedTable = state.confirmedTable ?: state.tables.firstOrNull()
            )
        }
    }

    fun closeTableOffcanvas() {
        _uiState.update { it.copy(showTableOffcanvas = false) }
    }

    fun selectTempTable(table: TableItem) {
        _uiState.update { it.copy(tempSelectedTable = table) }
    }

    fun confirmTableSelection() {
        _uiState.update { state ->
            posPreferenceManager.saveConfirmedTable(state.tempSelectedTable)
            state.copy(
                confirmedTable = state.tempSelectedTable,
                showTableOffcanvas = false
            )
        }
    }

    fun clearConfirmedTable() {
        posPreferenceManager.saveConfirmedTable(null)
        _uiState.update { state ->
            state.copy(
                confirmedTable = null,
                tempSelectedTable = null,
                showTableOffcanvas = false
            )
        }
    }

    fun processOrder() {
        posPreferenceManager.clearActivePosData()
        _uiState.update { state ->
            state.copy(
                cartItems = emptyList(),
                confirmedTable = null,
                tempSelectedTable = null,
                products = state.products.map { it.copy(isSelected = false) }
            )
        }
    }

    fun logoutAccount() {
        authRepository.clearSession()
    }

    fun logoutSystem() {
        authRepository.clearSession()
        outletRepository.clearOutletConfig()
        posPreferenceManager.clearActivePosData()
    }

    private companion object {
        val dummyCategories = listOf(
            "Semua",
            "Adds On",
            "Asian",
            "Beer",
            "Black Coffee Based",
            "Delivery",
            "Hot Drinks",
            "Indonesian Hype",
            "Manual Brew",
            "Milk Based",
            "Snack",
            "Sparkling & Juice"
        )

        val dummyProducts = listOf(
            PosProduct(1, "Soto Ayam", FOOD_CATEGORY, 35_000),
            PosProduct(2, "Steam rice", FOOD_CATEGORY, 8_000),
            PosProduct(3, "Original Tea", DRINK_CATEGORY, 10_000, hasAddon = true, variants = listOf("ICE", "HOT")),
            PosProduct(4, "Special Fried Rice", FOOD_CATEGORY, 40_000),
            PosProduct(5, "Javanese Fried Rice", FOOD_CATEGORY, 35_000),
            PosProduct(6, "Aqua 600ml", DRINK_CATEGORY, 6_000, warningLevel = 1),
            PosProduct(7, "Bintang Large", DRINK_CATEGORY, 65_000, warningLevel = 1),
            PosProduct(8, "Bintang Medium", DRINK_CATEGORY, 38_000, warningLevel = 1),
            PosProduct(9, "Banana Pancake", "Snack", 30_000, hasAddon = true, variants = listOf("Original", "Cokelat", "Keju")),
            PosProduct(10, "Cappucino", DRINK_CATEGORY, 30_000, hasAddon = true, variants = listOf("ICE", "HOT")),
            PosProduct(11, "Ginger Lemongrass Tea", DRINK_CATEGORY, 25_000),
            PosProduct(12, "Sate Ayam", FOOD_CATEGORY, 35_000),
            PosProduct(13, "Ayam Bakar", FOOD_CATEGORY, 50_000),
            PosProduct(14, "Special Juice", DRINK_CATEGORY, 30_000, hasAddon = true, variants = listOf("Alpukat", "Mangga", "Jeruk")),
            PosProduct(15, "Eggs", FOOD_CATEGORY, 15_000, hasAddon = true, variants = listOf("Dadar", "Mata Sapi", "Rebus")),
            PosProduct(16, "Draft Beer Bottle", DRINK_CATEGORY, 33_000, warningLevel = 1),
            PosProduct(17, "French Fries", "Snack", 28_000),
            PosProduct(18, "Rawon", FOOD_CATEGORY, 45_000),
            PosProduct(19, "Hongkong Fried Rice", FOOD_CATEGORY, 42_000),
            PosProduct(20, "Javanese Fried Noodle", FOOD_CATEGORY, 36_000)
        )

        val dummyTables = (1..20).map { num ->
            val formatted = if (num < 10) "0$num" else "$num"
            TableItem(id = "meja_$num", name = "Meja $formatted", status = "Tersedia")
        } + listOf(TableItem(id = "take_away", name = "Take Away", status = "Tersedia"))
    }
}
