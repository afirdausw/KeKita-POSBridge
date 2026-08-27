package devyana.kekita.posbridge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devyana.kekita.posbridge.data.repository.AuthRepository
import devyana.kekita.posbridge.data.repository.OutletRepository
import devyana.kekita.posbridge.data.repository.ProductRepository
import devyana.kekita.posbridge.data.repository.TransactionRepository
import devyana.kekita.posbridge.data.local.entity.TransactionEntity
import devyana.kekita.posbridge.data.local.entity.TransactionDetailEntity
import devyana.kekita.posbridge.data.local.entity.TransactionWithDetails
import devyana.kekita.posbridge.utils.OutletManager
import devyana.kekita.posbridge.utils.PosPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DUMMY_PRODUCT_TOTAL = 211

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
    val productType: String,
    val price: Int,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isSelected: Boolean = false,
    val hasAddon: Boolean = false,
    val variants: List<String> = emptyList(),
    val hasPpn: Boolean = false,
    val hasService: Boolean = false
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

enum class ServerSyncState {
    IDLE,
    SYNCING_DOWN,
    SYNCING_UP,
    PINGING,
    ERROR_OFFLINE
}

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
    val confirmedTable: TableItem? = null,
    // Order success dialog state
    val showOrderSuccessDialog: Boolean = false,
    val orderSuccessMessage: String = "",
    val lastSavedTransactionId: String? = null,
    // Sync state
    val syncState: ServerSyncState = ServerSyncState.IDLE
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
            .filter { it.product.productType.equals("Makanan", ignoreCase = true) }
            .sumOf { it.quantity }

    val drinkCount: Int
        get() = cartItems
            .filter { it.product.productType.equals("Minuman", ignoreCase = true) }
            .sumOf { it.quantity }

    val subtotal: Int
        get() = cartItems.sumOf { it.subtotal }

    val totalCartItemCount: Int
        get() = cartItems.sumOf { it.quantity }
}

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val outletRepository: OutletRepository,
    private val productRepository: devyana.kekita.posbridge.data.repository.ProductRepository,
    private val transactionRepository: devyana.kekita.posbridge.data.repository.TransactionRepository,
    private val posPreferenceManager: PosPreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePosUiState())
    val uiState: StateFlow<HomePosUiState> = _uiState.asStateFlow()

    val transactions: StateFlow<List<TransactionWithDetails>> = transactionRepository.getAllTransactionsWithDetails()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
        startClock()
        refreshInvoice()
        observeProducts()
        syncProducts()
    }

    private fun refreshInvoice() {
        viewModelScope.launch {
            val newInvoice = transactionRepository.generateInvoice()
            _uiState.update { it.copy(invoiceNumber = "Invoice #$newInvoice") }
        }
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

    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { entities ->
                val savedCart = posPreferenceManager.getCartItems()
                val activeProductIds = savedCart.map { it.product.id }.toSet()

                val products = entities.map { entity ->
                    val cat = if (entity.kategori.isBlank()) "Tanpa Kategori" else entity.kategori
                    PosProduct(
                        id = entity.idProduk,
                        name = entity.namaProduk,
                        category = cat,
                        productType = entity.jenisProduk,
                        price = entity.hargaJualDinein,
                        imageUrl = entity.gambarUrl,
                        isAvailable = entity.statusPenjualan == "Tersedia",
                        isSelected = activeProductIds.contains(entity.idProduk),
                        hasAddon = entity.varian.isNotEmpty(),
                        variants = entity.varian.map { it.namaVarian },
                        hasPpn = entity.hitungPpn.equals("Ya", ignoreCase = true),
                        hasService = entity.hitungService.equals("Ya", ignoreCase = true)
                    )
                }

                val categories = listOf("Semua") + products.map { it.category }.distinct().sorted()

                _uiState.update { state ->
                    state.copy(
                        products = products,
                        categories = categories,
                        totalProductCount = products.size
                    )
                }
            }
        }
    }

    fun pingServer() {
        val currentState = _uiState.value.syncState
        if (currentState == ServerSyncState.SYNCING_DOWN || currentState == ServerSyncState.SYNCING_UP || currentState == ServerSyncState.PINGING) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = ServerSyncState.PINGING) }
            val result = outletRepository.pingServer()
            if (result.isSuccess) {
                _uiState.update { it.copy(syncState = ServerSyncState.IDLE) }
            } else {
                _uiState.update { it.copy(syncState = ServerSyncState.ERROR_OFFLINE) }
            }
        }
    }

    fun syncProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = ServerSyncState.SYNCING_DOWN) }
            val result = productRepository.syncProducts()
            if (result.isSuccess) {
                _uiState.update { it.copy(syncState = ServerSyncState.IDLE) }
            } else {
                _uiState.update { it.copy(syncState = ServerSyncState.ERROR_OFFLINE) }
            }
        }
    }

    fun simulateSync() {
        if (_uiState.value.syncState == ServerSyncState.SYNCING_DOWN) return
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = ServerSyncState.SYNCING_DOWN) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(syncState = ServerSyncState.IDLE) }
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

        _uiState.value = HomePosUiState(
            homeData = homeData,
            tables = dummyTables, // Tables can remain dummy for now or be fetched later
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

    fun processOrder(): Boolean {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return false
        if (state.confirmedTable == null) return false

        val hasDrink = state.cartItems.any {
            it.product.productType.equals("Minuman", ignoreCase = true)
        }
        val hasFood = state.cartItems.any {
            it.product.productType.equals("Makanan", ignoreCase = true)
        } || (!hasDrink)

        val destinationText = when {
            hasDrink && hasFood -> "bar & dapur"
            hasDrink -> "bar"
            else -> "dapur"
        }

        val message = "Yeay, pesanan telah berhasil dikirim ke $destinationText"

        viewModelScope.launch {
            _uiState.update { it.copy(syncState = ServerSyncState.SYNCING_UP) }
            
            // Calculate Transaction
            val subtotal = state.cartItems.sumOf { it.subtotal }
            var totalService = 0
            var basePpn = 0
            state.cartItems.forEach { item ->
                val baseItem = item.subtotal.toDouble()
                val serviceItem = if (item.product.hasService) Math.round(baseItem * 0.05).toInt() else 0
                if (item.product.hasPpn) {
                    basePpn += (baseItem.toInt() + serviceItem)
                }
                totalService += serviceItem
            }
            val totalPpn = Math.round(basePpn * 0.10).toInt()
            val total = subtotal + totalService + totalPpn
            
            val sisa = total % 1000
            val rounded = when {
                sisa < 250 -> total - sisa
                sisa < 750 -> total - sisa + 500
                else -> total - sisa + 1000
            }
            val nilaiPembulatan = rounded - total
            val totalHarusDibayar = rounded
            
            val idTransaksi = java.util.UUID.randomUUID().toString()
            val invoice = state.invoiceNumber.removePrefix("Invoice #").trim()
            val tDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val tTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            
            val transactionEntity = TransactionEntity(
                idTransaksi = idTransaksi,
                tanggalTransaksi = tDate,
                jamTransaksi = tTime,
                customer = "Tamu",
                meja = state.confirmedTable?.name ?: "-",
                invoice = invoice,
                totalPesanan = subtotal,
                totalDiskonItem = 0,
                totalDiskonPotongan = 0,
                totalService = totalService,
                totalPpn = totalPpn,
                total = total,
                nilaiPembulatan = nilaiPembulatan,
                totalHarusDibayar = totalHarusDibayar,
                bayar = 0,
                statusTransaksi = "Menunggu Pembayaran",
                tipeTransaksi = "Normal",
                penggunaIdKasir = state.homeData?.username ?: "-",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val details = state.cartItems.map { item ->
                TransactionDetailEntity(
                    idDetailTransaksi = java.util.UUID.randomUUID().toString(),
                    transaksiId = idTransaksi,
                    produkId = item.product.id.toString(),
                    produkNama = item.product.name,
                    produkPpn = item.product.hasPpn,
                    produkService = item.product.hasService,
                    jumlahProduk = item.quantity,
                    jumlahTerbayar = 0,
                    hargaSatuanProduk = item.product.price,
                    subtotal = item.subtotal,
                    diskonItemPersen = 0,
                    diskonItem = 0,
                    diskonItemPotongan = 0,
                    statusItem = "konfirmasi",
                    catatanItem = item.note,
                    produkVarian = item.selectedVariant
                )
            }
            
            transactionRepository.saveTransaction(transactionEntity, details)

            delay(800) // Simulate network upload delay
            _uiState.update {
                it.copy(
                    showOrderSuccessDialog = true,
                    orderSuccessMessage = message,
                    lastSavedTransactionId = idTransaksi,
                    syncState = ServerSyncState.IDLE
                )
            }
        }
        return true
    }

    fun dismissOrderSuccessDialog() {
        posPreferenceManager.clearActivePosData()
        refreshInvoice()
        _uiState.update { state ->
            state.copy(
                showOrderSuccessDialog = false,
                orderSuccessMessage = "",
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

    fun updateTransactionToPaid(transactionId: String, paidAmount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val txDetails = transactionRepository.getTransactionById(transactionId) ?: return@launch
            val updatedTx = txDetails.transaction.copy(
                statusTransaksi = "Selesai",
                bayar = paidAmount,
                updatedAt = System.currentTimeMillis()
            )
            transactionRepository.updateTransaction(updatedTx)
        }
    }

    private companion object {
        val dummyTables = (1..20).map { num ->
            val formatted = if (num < 10) "0$num" else "$num"
            TableItem(id = "meja_$num", name = "Meja $formatted", status = "Tersedia")
        } + listOf(TableItem(id = "take_away", name = "Take Away", status = "Tersedia"))
    }
}
