package devyana.kekita.posbridge.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.data.local.entity.TransactionWithDetails
import devyana.kekita.posbridge.ui.components.DashedDivider
import devyana.kekita.posbridge.ui.payment.TransactionStatus
import java.text.NumberFormat
import java.util.Locale

import devyana.kekita.posbridge.utils.PosPreferenceManager
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton

import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentModal
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentItem

@Composable
fun TransactionScreenContent(
    transactions: List<TransactionWithDetails>,
    onNavigateToPos: () -> Unit = {},
    onProcessPayment: (String, Int) -> Unit = { _, _ -> }
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTxForDetail by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var selectedTxForCheckout by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, searchQuery) {
        if (searchQuery.isBlank()) transactions else {
            transactions.filter {
                it.transaction.invoice.contains(searchQuery, ignoreCase = true) ||
                it.transaction.meja.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    selectedTxForCheckout?.let { tx ->
        CheckoutPaymentModal(
            invoiceNo = tx.transaction.invoice,
            tableNo = tx.transaction.meja,
            items = tx.details.map { detail ->
                CheckoutPaymentItem(
                    id = detail.idDetailTransaksi,
                    qty = detail.jumlahProduk,
                    name = detail.produkNama,
                    variant = detail.produkVarian,
                    unitPrice = detail.hargaSatuanProduk,
                    note = detail.catatanItem,
                    hasPpn = detail.produkPpn,
                    hasService = detail.produkService
                )
            },
            onDismiss = { selectedTxForCheckout = null },
            onPaymentSuccess = { paidAmount ->
                onProcessPayment(tx.transaction.idTransaksi, paidAmount)
                selectedTxForCheckout = null 
            }
        )
    }

    selectedTxForDetail?.let { tx ->
        TransactionDetailModal(
            tx = tx,
            onDismiss = { selectedTxForDetail = null },
            onPrintClick = { 
                // Placeholder for print
            },
            onPayClick = {
                selectedTxForDetail = null
                selectedTxForCheckout = tx
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daftar Transaksi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Search Bar
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

            Spacer(modifier = Modifier.width(16.dp))

            // Cart Button
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
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_shopping_cart),
                            contentDescription = "Ke Halaman POS",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))

        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_file_text),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Pencarian tidak ditemukan." else "Belum ada transaksi di lokal.",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTransactions) { tx ->
                    TransactionItemCard(
                        tx = tx,
                        onClick = { selectedTxForDetail = tx }
                    )
                }
            }
        }
    }
}

fun formatTransactionDate(dateStr: String, timeStr: String): String {
    return try {
        val dateParts = dateStr.split("-")
        val timeParts = timeStr.split(":")
        if (dateParts.size >= 3 && timeParts.size >= 2) {
            "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}, ${timeParts[0]}:${timeParts[1]}"
        } else {
            "$dateStr, $timeStr"
        }
    } catch (e: Exception) {
        "$dateStr, $timeStr"
    }
}

@Composable
fun TransactionItemCard(tx: TransactionWithDetails, onClick: () -> Unit) {
    val t = tx.transaction
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    val statusObj = when (t.statusTransaksi.lowercase()) {
        "selesai" -> TransactionStatus.SELESAI
        "proses" -> TransactionStatus.PROSES
        "siap diantar" -> TransactionStatus.SIAP_DIANTAR
        "menunggu" -> TransactionStatus.MENUNGGU
        else -> TransactionStatus.BELUM_DIBAYAR
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kotak Rounded Ikon Struk (menyesuaikan warna meja PaymentCard)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_file_text),
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${t.invoice}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTransactionDate(t.tanggalTransaksi, t.jamTransaksi), 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
                
                Spacer(modifier = Modifier.width(6.dp))
                
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusObj.containerColor
                ) {
                    Text(
                        text = statusObj.label,
                        color = statusObj.contentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Info Row (Table & Cashier)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_armchair),
                        contentDescription = "Meja",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = t.meja, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_badge_check),
                        contentDescription = "Kasir",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = t.penggunaIdKasir, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            // Detail Items (show max 2, then "+ X lainnya")
            val maxDisplay = 2
            tx.details.take(maxDisplay).forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${detail.jumlahProduk}x ${detail.produkNama}", 
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatter.format(detail.subtotal).replace("Rp", "Rp "), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
            if (tx.details.size > maxDisplay) {
                Text(
                    text = "+ ${tx.details.size - maxDisplay} item lainnya",
                    fontSize = 11.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            
            // Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Text(
                    text = formatter.format(t.totalHarusDibayar).replace("Rp", "Rp "), 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailModal(
    tx: TransactionWithDetails,
    onDismiss: () -> Unit,
    onPrintClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val t = tx.transaction
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    val statusObj = when (t.statusTransaksi.lowercase()) {
        "selesai" -> TransactionStatus.SELESAI
        "proses" -> TransactionStatus.PROSES
        "siap diantar" -> TransactionStatus.SIAP_DIANTAR
        "menunggu" -> TransactionStatus.MENUNGGU
        else -> TransactionStatus.BELUM_DIBAYAR
    }
    
    val isPaid = t.statusTransaksi.equals("Selesai", ignoreCase = true)
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(420.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Detail Transaksi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    androidx.compose.material3.IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
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

                DetailInfoRow(label = "Nomor Meja", value = t.meja)
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Invoice", value = "#${t.invoice}")
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Tipe Pesanan", value = t.tipeTransaksi)
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Kasir", value = t.penggunaIdKasir)
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Tanggal & Waktu", value = formatTransactionDate(t.tanggalTransaksi, t.jamTransaksi))
                Spacer(modifier = Modifier.height(6.dp))
                DetailInfoRow(label = "Status", value = statusObj.label, valueColor = statusObj.contentColor)

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))

                val itemCount = tx.details.sumOf { it.jumlahProduk }
                Text(
                    text = "Daftar Item ($itemCount)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                tx.details.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.jumlahProduk}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.width(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.produkNama,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            if (!item.produkVarian.isNullOrBlank()) {
                                Text(
                                    text = "(${item.produkVarian})",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                            if (!item.catatanItem.isNullOrBlank()) {
                                Text(
                                    text = "Catatan: ${item.catatanItem}",
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
                            text = formatter.format(item.subtotal).replace("Rp", "Rp "),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                
                // Ringkasan Pembayaran
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PaymentSummaryRow(label = "Subtotal", value = formatter.format(t.totalPesanan).replace("Rp", "Rp "))
                    if (t.totalDiskonPotongan > 0) {
                        PaymentSummaryRow(
                            label = "Potongan",
                            value = "-${formatter.format(t.totalDiskonPotongan).replace("Rp", "Rp ")}",
                            valueColor = Color(0xFFEF4444)
                        )
                    }
                    if (t.totalDiskonItem > 0) {
                        PaymentSummaryRow(
                            label = "Diskon Item",
                            value = "-${formatter.format(t.totalDiskonItem).replace("Rp", "Rp ")}",
                            valueColor = Color(0xFFEF4444)
                        )
                    }
                    PaymentSummaryRow(label = "Service (5%)", value = formatter.format(t.totalService).replace("Rp", "Rp "))
                    PaymentSummaryRow(label = "PPN (10%)", value = formatter.format(t.totalPpn).replace("Rp", "Rp "))
                    PaymentSummaryRow(label = "Total", value = formatter.format(t.total).replace("Rp", "Rp "), isBold = true)
                    
                    if (t.nilaiPembulatan != 0) {
                        PaymentSummaryRow(label = "Pembulatan", value = formatter.format(t.nilaiPembulatan).replace("Rp", "Rp "))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Total Tagihan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = formatter.format(t.totalHarusDibayar).replace("Rp", "Rp "), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF475569)
                        )
                    ) {
                        Text(text = "Tutup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    androidx.compose.material3.Button(
                        onClick = onPrintClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Cetak", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (!isPaid) {
                        androidx.compose.material3.Button(
                            onClick = onPayClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Bayar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (isBold) Color(0xFF334155) else Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
