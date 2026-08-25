package devyana.kekita.posbridge.ui.payment

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.components.DashedDivider
import devyana.kekita.posbridge.ui.home.CartItem
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentModal
import devyana.kekita.posbridge.utils.PosPreferenceManager

enum class TransactionStatus(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val iconRes: Int
) {
    BELUM_DIBAYAR("Belum Dibayar", Color(0xFFFFEDD5), Color(0xFFC2410C), R.drawable.ic_lucide_credit_card),
    MENUNGGU("Menunggu", Color(0xFFDBEAFE), Color(0xFF1D4ED8), R.drawable.ic_lucide_history),
    PROSES("Proses", Color(0xFFFEF3C7), Color(0xFFB45309), R.drawable.ic_lucide_refresh_cw),
    SIAP_DIANTAR("Siap Diantar", Color(0xFFCCFBF1), Color(0xFF0F766E), R.drawable.ic_lucide_badge_check),
    SELESAI("Selesai", Color(0xFFD1FAE5), Color(0xFF047857), R.drawable.ic_lucide_check)
}

data class PaymentTransactionOrder(
    val invoiceNo: String,
    val tableNo: String,
    val orderType: String = "Dine In",
    val transactionDate: String = "03-08-2026",
    val transactionTime: String = "18:22",
    val status: TransactionStatus,
    val items: List<CheckoutPaymentItem>,
    val itemDiscount: Int = 0
) {
    val subtotal: Int get() = items.sumOf { it.totalOriginal }
    val itemCount: Int get() = items.sumOf { it.qty }
    val isPaid: Boolean get() = status == TransactionStatus.SELESAI

    val netSubtotal: Int get() = (subtotal - itemDiscount).coerceAtLeast(0)
    val serviceAmount: Int get() = (netSubtotal * 0.05).toInt()
    val taxAmount: Int get() = ((netSubtotal + serviceAmount) * 0.10).toInt()
    val rawTotal: Int get() = netSubtotal + serviceAmount + taxAmount
    val rounding: Int get() = 0
    val grandTotal: Int get() = rawTotal + rounding
    val totalAmount: Int get() = grandTotal
}

@Composable
fun PaymentScreenContent(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onNavigateToPos: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    devyana.kekita.posbridge.ui.components.PosPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Semua, 1 = Belum Dibayar, 2 = Sudah Dibayar
    var searchQuery by remember { mutableStateOf("") }

    var isSearchFocused by remember { mutableStateOf(false) }

    var selectedOrderForCheckout by remember { mutableStateOf<PaymentTransactionOrder?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<PaymentTransactionOrder?>(null) }

    val sampleOrders = remember {
        listOf(
            PaymentTransactionOrder(
                invoiceNo = "#260803001",
                tableNo = "10",
                orderType = "Dine In",
                transactionDate = "03-08-2026",
                transactionTime = "18:22",
                status = TransactionStatus.BELUM_DIBAYAR,
                items = listOf(
                    CheckoutPaymentItem("1", 1, "Soto Ayam", null, 35_000),
                    CheckoutPaymentItem("2", 1, "Steam rice", null, 8_000),
                    CheckoutPaymentItem("3", 1, "Aqua 600ml", null, 6_000),
                    CheckoutPaymentItem("4", 1, "Eggs", "OMELETE", 15_000)
                )
            ),
            PaymentTransactionOrder(
                invoiceNo = "#260803002",
                tableNo = "04",
                orderType = "Dine In",
                transactionDate = "03-08-2026",
                transactionTime = "18:20",
                status = TransactionStatus.PROSES,
                items = listOf(
                    CheckoutPaymentItem("1", 1, "Special Fried Rice", null, 35_000),
                    CheckoutPaymentItem("2", 1, "Original Tea", "ICE", 10_000)
                )
            ),
            PaymentTransactionOrder(
                invoiceNo = "#260803003",
                tableNo = "02",
                orderType = "Takeaway",
                transactionDate = "03-08-2026",
                transactionTime = "18:35",
                status = TransactionStatus.SIAP_DIANTAR,
                items = listOf(
                    CheckoutPaymentItem("1", 2, "Ayam Bakar", null, 50_000),
                    CheckoutPaymentItem("2", 1, "Cappucino", "HOT", 30_000),
                    CheckoutPaymentItem("3", 2, "Steam rice", null, 8_000),
                    CheckoutPaymentItem("4", 1, "Aqua 600ml", null, 6_000)
                )
            ),
            PaymentTransactionOrder(
                invoiceNo = "#260803000",
                tableNo = "08",
                orderType = "Dine In",
                transactionDate = "03-08-2026",
                transactionTime = "17:40",
                status = TransactionStatus.SELESAI,
                items = listOf(
                    CheckoutPaymentItem("1", 2, "Ayam Bakar", null, 50_000),
                    CheckoutPaymentItem("2", 1, "Cappucino", "HOT", 30_000),
                    CheckoutPaymentItem("3", 1, "Steam rice", null, 5_000)
                )
            )
        )
    }

    val filteredOrders = remember(selectedTab, searchQuery, sampleOrders) {
        sampleOrders.filter { order ->
            val matchesTab = when (selectedTab) {
                0 -> !order.isPaid // Default: Belum Dibayar
                1 -> order.isPaid  // Sudah Dibayar
                2 -> true          // Semua
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    order.invoiceNo.contains(searchQuery, ignoreCase = true) ||
                    order.tableNo.contains(searchQuery, ignoreCase = true)

            matchesTab && matchesSearch
        }
    }

    // Modal Pembayaran Checkout jika ada transaksi yang dipilih untuk dibayar
    selectedOrderForCheckout?.let { order ->
        CheckoutPaymentModal(
            invoiceNo = order.invoiceNo,
            tableNo = order.tableNo,
            items = order.items,
            onDismiss = { selectedOrderForCheckout = null },
            onPaymentSuccess = { selectedOrderForCheckout = null }
        )
    }

    // Modal Detail Transaksi jika diklik "Detail"
    selectedOrderForDetail?.let { order ->
        TransactionDetailModal(
            order = order,
            onDismiss = { selectedOrderForDetail = null },
            onPayClick = {
                selectedOrderForDetail = null
                selectedOrderForCheckout = order
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            .padding(20.dp)
    ) {
        // Top Bar: Judul (Kiri, Tanpa Emoji, Vertical Align Top) & Cart Circle Button (Kanan, Smaller CircleShape, Click -> POS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Status & Riwayat Pembayaran",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Cart Circle Icon with Item Count Badge from SharedPreferences
            val posPrefManager = remember(context) { PosPreferenceManager(context) }
            val cartItemCount = remember(context) {
                posPrefManager.getCartItems().sumOf { it.quantity }
            }

            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.clickable(onClick = onNavigateToPos)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_shopping_cart),
                            contentDescription = "Ke Halaman POS",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Row 2: 3 Category Tabs (Kiri) & Search Bar (Sejajar di Paling Kanan!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3 TABS SELECTION: Belum Dibayar (Default), Sudah Dibayar, Semua
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val unpaidCount = sampleOrders.count { !it.isPaid }
                val paidCount = sampleOrders.count { it.isPaid }
                val totalCount = sampleOrders.size

                CategoryChip(
                    text = "Belum Dibayar ($unpaidCount)",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                CategoryChip(
                    text = "Sudah Dibayar ($paidCount)",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                CategoryChip(
                    text = "Semua ($totalCount)",
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // REVISED SEARCH BAR: BasicTextField aligned on FAR RIGHT side of Tab row!
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                modifier = Modifier.onFocusChanged { isSearchFocused = it.isFocused },
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                decorationBox = { innerTextField ->
                    Surface(
                        modifier = Modifier
                            .width(280.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(
                            if (isSearchFocused) 1.5.dp else 1.dp,
                            if (isSearchFocused) Color(0xFF2563EB) else Color(0xFFCBD5E1)
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
                                contentDescription = "Cari",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari invoice, meja...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                        )
                                    )
                                }
                                innerTextField()
                            }

                            if (searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lucide_x),
                                        contentDescription = "Hapus",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada transaksi dalam kategori ini",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            // GRID CARD LAYOUT FOR ORDERS
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders, key = { it.invoiceNo }) { order ->
                    PaymentOrderCardGrid(
                        order = order,
                        onDetailClick = { selectedOrderForDetail = order },
                        onPayClick = { selectedOrderForCheckout = order }
                    )
                }
            }
        }
        }
    }
}

// CategoryChip matching POS Category style (Height 40.dp matching search bar)
@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFF2563EB) else Color(0xFFDBEAFE),
        contentColor = if (selected) Color.White else Color(0xFF1D4ED8)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
    }
}

@Composable
private fun PaymentOrderCardGrid(
    order: PaymentTransactionOrder,
    onDetailClick: () -> Unit,
    onPayClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER BAR: Table Badge (Uniform color for all), Invoice, Date & Time, Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Kotak Rounded Nomor Meja (Warna seragam biru POS)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFD97706)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = order.tableNo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Invoice & Tanggal/Jam (Tanpa Nama Pelanggan)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.invoiceNo} • ${order.orderType}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${order.transactionDate}, ${order.transactionTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = order.status.containerColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = order.status.iconRes),
                            contentDescription = null,
                            tint = order.status.contentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = order.status.contentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            // RINGKASAN PEMBAYARAN (SUMMARY PAYMENT)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PaymentSummaryRow(label = "Jumlah Item", value = "${order.itemCount} Item")
                PaymentSummaryRow(label = "Subtotal", value = formatRupiah(order.subtotal))
                PaymentSummaryRow(
                    label = "Diskon Item",
                    value = if (order.itemDiscount > 0) "-${formatRupiah(order.itemDiscount)}" else "Rp 0",
                    valueColor = if (order.itemDiscount > 0) Color(0xFFEF4444) else Color(0xFF1E293B)
                )
                PaymentSummaryRow(label = "Service (5%)", value = formatRupiah(order.serviceAmount))
                PaymentSummaryRow(label = "PPN (10%)", value = formatRupiah(order.taxAmount))
                PaymentSummaryRow(label = "Total", value = formatRupiah(order.rawTotal), isBold = true)
                PaymentSummaryRow(label = "Pembulatan", value = formatRupiah(order.rounding))
            }

            Spacer(modifier = Modifier.height(10.dp))
            DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            // GRAND TOTAL ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grand Total",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatRupiah(order.grandTotal),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ACTION BUTTONS: Detail | Bayar (FULL WIDTH DETAIL IF ALREADY PAID)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF475569)
                    ),
                    elevation = null
                ) {
                    Text(
                        text = "Detail",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!order.isPaid) {
                    Button(
                        onClick = onPayClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.White
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = "Bayar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailModal(
    order: PaymentTransactionOrder,
    onDismiss: () -> Unit,
    onPayClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(420.dp)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Detail Transaksi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_x),
                            contentDescription = "Tutup",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                // Info Rows (Tanpa Pelanggan)
                DetailInfoRow(label = "Nomor Meja", value = order.tableNo)
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Invoice", value = order.invoiceNo)
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Tipe Pesanan", value = order.orderType)
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Tanggal & Waktu", value = "${order.transactionDate}, ${order.transactionTime}")
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Status", value = order.status.label, valueColor = order.status.contentColor)

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Daftar Item (${order.itemCount})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.qty}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.width(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            if (!item.variant.isNullOrBlank()) {
                                Text(
                                    text = "(${item.variant})",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                        }
                        Text(
                            text = formatRupiah(item.totalOriginal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Total Tagihan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = formatRupiah(order.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF475569)
                        )
                    ) {
                        Text(text = "Tutup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (!order.isPaid) {
                        Button(
                            onClick = onPayClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Bayar Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B)
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PaymentSummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B),
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun formatRupiah(value: Int): String {
    return "Rp ${"%,d".format(value).replace(",", ".")}"
}
