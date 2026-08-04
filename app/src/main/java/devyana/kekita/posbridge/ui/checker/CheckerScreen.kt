package devyana.kekita.posbridge.ui.checker

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
import devyana.kekita.posbridge.ui.payment.PaymentTransactionOrder
import devyana.kekita.posbridge.ui.payment.TransactionStatus
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentItem
import devyana.kekita.posbridge.utils.PosPreferenceManager

@Composable
fun CheckerScreenContent(
    onNavigateToPos: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Belum Diproses, 1 = Sedang Dikerjakan, 2 = Siap Diantar, 3 = Semua
    var searchQuery by remember { mutableStateOf("") }

    var isSearchFocused by remember { mutableStateOf(false) }

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
                    CheckoutPaymentItem("1", 1, "Soto Ayam", null, 35_000, "Jangan pakai seledri"),
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
                    CheckoutPaymentItem("1", 1, "Special Fried Rice", null, 35_000, "Pedas sedang"),
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
                    CheckoutPaymentItem("2", 1, "Cappucino", "HOT", 30_000, "Extra sugar"),
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
                0 -> order.status == TransactionStatus.SIAP_DIANTAR // Default: Siap Diantar
                1 -> order.status == TransactionStatus.PROSES // Sedang Dikerjakan
                2 -> order.status == TransactionStatus.BELUM_DIBAYAR || order.status == TransactionStatus.MENUNGGU // Belum Diproses
                3 -> true // Semua
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    order.invoiceNo.contains(searchQuery, ignoreCase = true) ||
                    order.tableNo.contains(searchQuery, ignoreCase = true)

            matchesTab && matchesSearch
        }
    }

    // Modal Detail Transaksi jika diklik "Detail"
    selectedOrderForDetail?.let { order ->
        CheckerTransactionDetailModal(
            order = order,
            onDismiss = { selectedOrderForDetail = null },
            onCheckerClick = {
                selectedOrderForDetail = null
                Toast.makeText(context, "Buka modal checker untuk invoice ${order.invoiceNo}", Toast.LENGTH_SHORT).show()
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
        // Top Bar: Judul (Order Checker Bar & Kitchen) & Cart Circle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Order Checker (Bar & Kitchen)",
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val siapDiantarCount = sampleOrders.count { it.status == TransactionStatus.SIAP_DIANTAR }
                val dikerjakanCount = sampleOrders.count { it.status == TransactionStatus.PROSES }
                val belumDiprosesCount = sampleOrders.count { it.status == TransactionStatus.BELUM_DIBAYAR || it.status == TransactionStatus.MENUNGGU }
                val totalCount = sampleOrders.size

                CategoryChip(
                    text = "Siap Diantar ($siapDiantarCount)",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                CategoryChip(
                    text = "Sedang Dikerjakan ($dikerjakanCount)",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                CategoryChip(
                    text = "Belum Diproses ($belumDiprosesCount)",
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                CategoryChip(
                    text = "Semua ($totalCount)",
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
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
                    text = "Tidak ada pesanan dalam kategori ini",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            // GRID CARD LAYOUT FOR CHECKER ORDERS
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders, key = { it.invoiceNo }) { order ->
                    CheckerOrderCardGrid(
                        order = order,
                        onDetailClick = { selectedOrderForDetail = order },
                        onCheckerClick = {
                            Toast.makeText(context, "Klik Checker untuk invoice ${order.invoiceNo}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

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
private fun CheckerOrderCardGrid(
    order: PaymentTransactionOrder,
    onDetailClick: () -> Unit,
    onCheckerClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER BAR: Table Badge, Invoice, Date & Time, Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Kotak Rounded Nomor Meja
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F766E)), // Same as checker button color
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

                // Invoice & Tanggal/Jam
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

            // TABEL ITEM (Items, Qty)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = "Items",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Qty",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }

            // Display up to 3 items inside card table
            order.items.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(vertical = 1.dp)) {
                        Text(
                            text = item.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        if (!item.variant.isNullOrBlank()) {
                            Text(
                                text = "(${item.variant})",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                        if (!item.note.isNullOrBlank()) {
                            Text(
                                text = "Catatan: ${item.note}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD97706),
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                    Text(
                        text = item.qty.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            if (order.items.size > 3) {
                Text(
                    text = "+ ${order.items.size - 3} item lainnya...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ACTION BUTTONS: Detail | Checker (GANTI DARI BAYAR KE CHECKER)
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
                        onClick = onCheckerClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E),
                            contentColor = Color.White
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = "Checker",
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
private fun CheckerTransactionDetailModal(
    order: PaymentTransactionOrder,
    onDismiss: () -> Unit,
    onCheckerClick: () -> Unit
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
                        text = "Detail Pesanan Checker",
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
                            if (!item.note.isNullOrBlank()) {
                                Text(
                                    text = "Catatan: ${item.note}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD97706),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatRupiah(item.totalOriginal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
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
                            onClick = onCheckerClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F766E),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Checker", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

private fun formatRupiah(value: Int): String {
    return "Rp ${"%,d".format(value).replace(",", ".")}"
}
