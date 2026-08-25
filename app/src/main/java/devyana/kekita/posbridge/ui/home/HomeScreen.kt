package devyana.kekita.posbridge.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.components.ActionCircleButton
import devyana.kekita.posbridge.ui.components.DashedDivider
import devyana.kekita.posbridge.ui.components.PosHeader
import devyana.kekita.posbridge.ui.components.SummaryRow
import devyana.kekita.posbridge.ui.components.dashedBorder
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentItem
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    viewModel: HomeViewModel,
    onNavigateToPayment: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var showCheckoutModal by remember { mutableStateOf(false) }

    // Dialog Sukses Pesanan
    if (uiState.showOrderSuccessDialog) {
        OrderSuccessDialog(
            message = uiState.orderSuccessMessage,
            onDismiss = viewModel::dismissOrderSuccessDialog,
            onNavigateToPayment = {
                showCheckoutModal = true
            }
        )
    }

    // Modal Checkout Pembayaran
    if (showCheckoutModal) {
        val checkoutItems = remember(uiState.cartItems) {
            uiState.cartItems.map {
                CheckoutPaymentItem(
                    id = it.id,
                    qty = it.quantity,
                    name = it.product.name,
                    variant = it.selectedVariant,
                    unitPrice = it.product.price
                )
            }
        }
        CheckoutPaymentModal(
            invoiceNo = uiState.invoiceNumber,
            tableNo = uiState.confirmedTable?.name ?: "-",
            items = checkoutItems.ifEmpty {
                listOf(
                    CheckoutPaymentItem("1", 1, "Soto Ayam", null, 35_000),
                    CheckoutPaymentItem("2", 1, "Steam rice", null, 8_000),
                    CheckoutPaymentItem("3", 1, "Aqua 600ml", null, 6_000),
                    CheckoutPaymentItem("4", 1, "Eggs", "OMELETE", 15_000)
                )
            },
            onDismiss = {
                showCheckoutModal = false
                viewModel.dismissOrderSuccessDialog()
            },
            onPaymentSuccess = {
                showCheckoutModal = false
                viewModel.dismissOrderSuccessDialog()
            }
        )
    }

    // Modal Popup Variant produk
    uiState.selectedProductForVariant?.let { product ->
        VariantSelectionDialog(
            product = product,
            onVariantSelected = { variant -> viewModel.selectVariantAndAdd(product, variant) },
            onDismiss = viewModel::dismissVariantDialog
        )
    }

    // Modal Popup Catatan Item
    uiState.selectedCartItemForNote?.let { item ->
        NoteInputDialog(
            cartItem = item,
            inputText = uiState.noteInputText,
            onInputTextChange = viewModel::updateNoteInputText,
            onSave = viewModel::saveNote,
            onDismiss = viewModel::dismissNoteDialog
        )
    }

    // Offcanvas Top Tentukan Meja (Top Sheet full width dari atas)
    if (uiState.showTableOffcanvas) {
        TableSelectionOffcanvas(
            tables = uiState.tables,
            tempSelectedTable = uiState.tempSelectedTable,
            confirmedTable = uiState.confirmedTable,
            onSelectTempTable = viewModel::selectTempTable,
            onConfirm = viewModel::confirmTableSelection,
            onClearTable = viewModel::clearConfirmedTable,
            onDismiss = viewModel::closeTableOffcanvas
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 12.dp, top = 12.dp, end = 10.dp)
        ) {
            PosHeader(
                invoiceNumber = uiState.invoiceNumber,
                businessDate = uiState.businessDate,
                userName = uiState.homeData?.displayName ?: "Owner",
                outletName = uiState.homeData?.outletName ?: "KeKita"
            )

            Spacer(modifier = Modifier.height(10.dp))

            PosSearchAndTableBar(
                query = uiState.searchQuery,
                confirmedTable = uiState.confirmedTable,
                onQueryChange = viewModel::updateSearchQuery,
                onTableClick = {
                    focusManager.clearFocus()
                    viewModel.openTableOffcanvas()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CategoryFilterRow(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                totalProductCount = uiState.totalProductCount,
                onCategorySelected = { category ->
                    focusManager.clearFocus()
                    viewModel.selectCategory(category)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            devyana.kekita.posbridge.ui.components.PosPullToRefreshBox(
                isRefreshing = uiState.syncState == devyana.kekita.posbridge.ui.home.ServerSyncState.SYNCING_DOWN,
                onRefresh = { viewModel.syncProducts() },
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                ProductGrid(
                    products = uiState.filteredProducts,
                    cartItems = uiState.cartItems,
                    onProductClick = { product ->
                        focusManager.clearFocus()
                        viewModel.onProductClick(product)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        OrderPanel(
            uiState = uiState,
            onIncrement = viewModel::incrementCartItem,
            onDecrement = viewModel::decrementCartItem,
            onRemoveItem = viewModel::removeCartItem,
            onClearCart = viewModel::clearCart,
            onOpenNote = viewModel::openNoteDialog,
            onProcessOrder = {
                if (uiState.cartItems.isEmpty()) {
                    return@OrderPanel
                }
                if (uiState.confirmedTable == null) {
                    Toast.makeText(context, "Silakan tentukan meja terlebih dahulu", Toast.LENGTH_SHORT).show()
                    return@OrderPanel
                }
                viewModel.processOrder()
            },
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .padding(top = 8.dp, end = 10.dp, bottom = 8.dp)
        )
    }
}

// Lucide Icons: Search, Close/X, Armchair
@Composable
private fun PosSearchAndTableBar(
    query: String,
    confirmedTable: TableItem?,
    onQueryChange: (String) -> Unit,
    onTableClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surface,
            border = BorderStroke(
                if (isSearchFocused) 1.5.dp else 1.dp,
                if (isSearchFocused) Color(0xFF2563EB) else Color(0xFFE2E8F0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_search),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Cari Produk",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isSearchFocused = it.isFocused },
                        textStyle = TextStyle(
                            color = colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable {
                                onQueryChange("")
                                focusManager.clearFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_x),
                            contentDescription = "Hapus",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .height(46.dp)
                .clickable(onClick = onTableClick),
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surface,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_armchair),
                    contentDescription = "Tentukan Meja",
                    tint = if (confirmedTable != null) Color(0xFF2563EB) else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                if (confirmedTable != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = confirmedTable.name,
                        color = Color(0xFF2563EB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Lucide Icons: Chevron Left, Chevron Right
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    totalProductCount: Int,
    onCategorySelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    text = category,
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Total $totalProductCount Item produk",
            color = colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF2563EB) else Color(0xFFDBEAFE),
        contentColor = if (selected) Color.White else Color(0xFF1D4ED8)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

// Lucide Icon: SearchX
@Composable
private fun ProductGrid(
    products: List<PosProduct>,
    cartItems: List<CartItem>,
    onProductClick: (PosProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_search_x),
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Produk tidak ditemukan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Coba kata kunci pencarian atau kategori lain",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val columns = if (maxWidth < 980.dp) 3 else 4
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    val quantityInCart = cartItems
                        .filter { it.product.id == product.id }
                        .sumOf { it.quantity }

                    ProductCard(
                        product = product,
                        quantityInCart = quantityInCart,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

// Lucide Icons: Tag, Image
@Composable
private fun ProductCard(
    product: PosProduct,
    quantityInCart: Int,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSelected = quantityInCart > 0
    val borderModifier = if (isSelected) {
        Modifier.border(1.dp, Color(0xFF849257), RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Surface(
        modifier = Modifier
            .height(162.dp)
            .then(borderModifier)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFE2E6D8) else colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    text = product.name,
                    color = colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.category,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvailabilityBadge(available = product.isAvailable)
                    if (product.variants.isNotEmpty() || product.hasAddon) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_tag),
                            contentDescription = "Varian",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        StockDots(hasPpn = product.hasPpn, hasService = product.hasService)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatRupiah(product.price),
                            color = colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_image),
                            contentDescription = null,
                            tint = colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (quantityInCart > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quantityInCart.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityBadge(available: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (available) colorScheme.tertiaryContainer else colorScheme.errorContainer
    ) {
        Text(
            text = if (available) "Tersedia" else "Habis",
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp),
            color = if (available) colorScheme.tertiary else colorScheme.error,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StockDots(hasPpn: Boolean, hasService: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (hasPpn) colorScheme.tertiary else colorScheme.error)
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (hasService) colorScheme.tertiary else colorScheme.error)
        )
    }
}

@Composable
private fun VariantSelectionDialog(
    product: PosProduct,
    onVariantSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Varian untuk :",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                product.variants.forEach { variant ->
                    Button(
                        onClick = { onVariantSelected(variant) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF6FF),
                            contentColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text(
                            text = variant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64748B),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Tutup",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteInputDialog(
    cartItem: CartItem,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(340.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Catatan untuk :",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cartItem.product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )
                if (!cartItem.selectedVariant.isNullOrEmpty()) {
                    Text(
                        text = cartItem.selectedVariant,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text("Tulis catatan...") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF059669),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF059669),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF64748B),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Tutup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Simpan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// Lucide Icon: Close/X
@Composable
private fun TableSelectionOffcanvas(
    tables: List<TableItem>,
    tempSelectedTable: TableItem?,
    confirmedTable: TableItem?,
    onSelectTempTable: (TableItem) -> Unit,
    onConfirm: () -> Unit,
    onClearTable: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tentukan Meja",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_x),
                                contentDescription = "Tutup",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(250.dp)
                    ) {
                        items(tables, key = { it.id }) { table ->
                            val isSelected = table.id == tempSelectedTable?.id
                            val cardModifier = if (isSelected) {
                                Modifier
                                    .height(70.dp)
                                    .clickable { onSelectTempTable(table) }
                            } else {
                                Modifier
                                    .height(70.dp)
                                    .dashedBorder(color = Color(0xFFCBD5E1), cornerRadius = 12.dp)
                                    .clickable { onSelectTempTable(table) }
                            }

                            Surface(
                                modifier = cardModifier,
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFB5B396) else Color.White,
                                border = if (isSelected) BorderStroke(1.dp, Color(0xFF6B6850)) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = buildAnnotatedString {
                                            if (table.name.startsWith("Meja ")) {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp)) {
                                                    append("Meja ")
                                                }
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                                                    append(table.name.removePrefix("Meja "))
                                                }
                                            } else {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                                                    append(table.name)
                                                }
                                            }
                                        },
                                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = table.status,
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (confirmedTable != null || tempSelectedTable != null) {
                            Button(
                                onClick = onClearTable,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEE2E2),
                                    contentColor = Color(0xFFDC2626)
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(text = "Hapus Meja", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF4B5563)
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(text = "Tutup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD1FAE5),
                                contentColor = Color(0xFF10B981)
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(text = "Pilih Meja", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Lucide Icon: RefreshCw
@Composable
private fun OrderPanel(
    uiState: HomePosUiState,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onOpenNote: (CartItem) -> Unit,
    onProcessOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item Pesanan",
                    color = colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.totalCartItemCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.totalCartItemCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                ActionCircleButton(
                    iconRes = R.drawable.ic_lucide_refresh_cw,
                    contentDescription = "Reset",
                    backgroundColor = Color(0xFFFEE2E2),
                    iconColor = Color(0xFFEF4444),
                    buttonSize = 26.dp,
                    iconSize = 16.dp,
                    onClick = onClearCart
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.cartItems.isEmpty()) {
                EmptyOrder(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(uiState.cartItems, key = { _, item -> item.id }) { index, item ->
                        CartItemRow(
                            item = item,
                            showDivider = index < uiState.cartItems.lastIndex,
                            onIncrement = { onIncrement(item.id) },
                            onDecrement = { onDecrement(item.id) },
                            onRemove = { onRemoveItem(item.id) },
                            onNoteClick = { onOpenNote(item) }
                        )
                    }
                }
            }

            OrderSummary(
                uiState = uiState,
                onProcessOrder = onProcessOrder
            )
        }
    }
}

@Composable
private fun RoundIconButton(iconRes: Int, enabled: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) colorScheme.surface else colorScheme.outline.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (enabled) colorScheme.onSurfaceVariant else colorScheme.surface,
            modifier = Modifier.size(16.dp)
        )
    }
}

// Lucide Icon: ShoppingCart
@Composable
private fun EmptyOrder(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_shopping_cart),
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tambahkan item",
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// Lucide Icons: Trash, Minus, Plus, FileText
@Composable
private fun CartItemRow(
    item: CartItem,
    showDivider: Boolean = true,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    onNoteClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val hasNote = !item.note.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = item.product.name,
            color = colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!item.selectedVariant.isNullOrEmpty()) {
            Text(
                text = "Varian: ${item.selectedVariant}",
                color = colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        Text(
            text = "@ ${formatRupiah(item.product.price)}",
            color = colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )

        if (hasNote) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_file_text),
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.note ?: "",
                    color = Color(0xFFD97706),
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.quantity <= 1) {
                ActionCircleButton(
                    iconRes = R.drawable.ic_lucide_trash,
                    contentDescription = "Hapus",
                    backgroundColor = Color(0xFFFEE2E2),
                    iconColor = Color(0xFFEF4444),
                    onClick = onRemove
                )
            } else {
                ActionCircleButton(
                    iconRes = R.drawable.ic_lucide_minus,
                    contentDescription = "Kurang",
                    backgroundColor = Color(0xFFDBEAFE),
                    iconColor = Color(0xFF2563EB),
                    onClick = onDecrement
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.quantity.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            ActionCircleButton(
                iconRes = R.drawable.ic_lucide_plus,
                contentDescription = "Tambah",
                backgroundColor = Color(0xFFDBEAFE),
                iconColor = Color(0xFF2563EB),
                onClick = onIncrement
            )

            Spacer(modifier = Modifier.width(8.dp))

            ActionCircleButton(
                iconRes = R.drawable.ic_lucide_file_text,
                contentDescription = "Catatan",
                backgroundColor = if (hasNote) Color(0xFFFBBF24) else Color(0xFFFEF3C7),
                iconColor = if (hasNote) Color(0xFF78350F) else Color(0xFFD97706),
                onClick = onNoteClick
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatRupiah(item.subtotal),
                color = colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showDivider) {
            Spacer(modifier = Modifier.height(12.dp))
            DashedDivider(
                color = colorScheme.outline.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OrderSummary(
    uiState: HomePosUiState,
    onProcessOrder: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        DashedDivider(
            color = colorScheme.outline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        SummaryRow(label = "Makanan :", value = uiState.foodCount.toString())
        Spacer(modifier = Modifier.height(6.dp))
        SummaryRow(label = "Minuman :", value = uiState.drinkCount.toString())
        Spacer(modifier = Modifier.height(6.dp))
        SummaryRow(label = "Sub Total :", value = formatRupiah(uiState.subtotal))
        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onProcessOrder,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Proses Pesanan",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatRupiah(value: Int): String {
    return "Rp ${"%,d".format(value).replace(",", ".")}"
}

@Composable
private fun OrderSuccessDialog(
    message: String,
    onDismiss: () -> Unit,
    onNavigateToPayment: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(400.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Green Scalloped Badge Check Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_badge_check),
                        contentDescription = "Sukses",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                DashedDivider(
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF8FAFC),
                            contentColor = Color(0xFF64748B)
                        ),
                        elevation = null
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_x),
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tutup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToPayment()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3E8FF),
                            contentColor = Color(0xFF8B5CF6)
                        ),
                        elevation = null
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_arrow_right),
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ke Pembayaran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
