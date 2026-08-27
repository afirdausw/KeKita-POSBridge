package devyana.kekita.posbridge.ui.payment.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.components.DashedDivider

enum class PaymentMethod(val label: String, val iconRes: Int) {
    TUNAI("Tunai", R.drawable.ic_lucide_banknote),
    QRIS("QRIS", R.drawable.ic_lucide_qr_code),
    EDC("EDC", R.drawable.ic_lucide_credit_card),
    TRANSFER("Transfer", R.drawable.ic_lucide_arrow_right_left)
}

enum class BillMode(val label: String, val iconRes: Int) {
    NORMAL("Normal", R.drawable.ic_lucide_file_text),
    SPLIT_BILL("Split Bill", R.drawable.ic_lucide_package)
}

data class CheckoutPaymentItem(
    val id: String,
    val qty: Int,
    val name: String,
    val variant: String? = null,
    val unitPrice: Int,
    val note: String? = null,
    val hasPpn: Boolean = true,
    val hasService: Boolean = true
) {
    val totalOriginal: Int = unitPrice * qty
}

data class CompletedPaymentResult(
    val invoiceNo: String,
    val tableNo: String,
    val changeAmount: Int,
    val isSplitBill: Boolean,
    val hasUnpaidItems: Boolean
)

@Composable
fun CheckoutPaymentModal(
    invoiceNo: String = "Invoice #260803001",
    tableNo: String = "10",
    items: List<CheckoutPaymentItem> = listOf(
        CheckoutPaymentItem("1", 1, "Soto Ayam", null, 35_000),
        CheckoutPaymentItem("2", 1, "Steam rice", null, 8_000),
        CheckoutPaymentItem("3", 1, "Aqua 600ml", null, 6_000),
        CheckoutPaymentItem("4", 1, "Eggs", "OMELETE", 15_000)
    ),
    onDismiss: () -> Unit,
    onPaymentSuccess: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedBillMode by remember { mutableStateOf(BillMode.NORMAL) }
    var manualApplyItemDiscount by remember { mutableStateOf(false) }

    // Split Bill states
    val checkedSplitItems = remember { mutableStateListOf<String>() }
    val paidSplitItemIds = remember { mutableStateListOf<String>() }

    // Custom Cash ("Lainnya") state
    var showCustomCashDialog by remember { mutableStateOf(false) }
    var customCashAmount by remember { mutableStateOf<Int?>(null) }

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.TUNAI) }

    // Initial paidAmount starts as 0 (No cash suggestion checked by default!)
    var paidAmount by remember { mutableIntStateOf(0) }

    // Result dialog state when payment succeeds
    var activeCompletionResult by remember { mutableStateOf<CompletedPaymentResult?>(null) }

    // Item discounts map (item id to percentage)
    val itemDiscountsPercent = remember { mutableStateMapOf<String, Int>() }

    // Controlled applyItemDiscount: Forced ON if Split Bill is active!
    val isSplitBill = selectedBillMode == BillMode.SPLIT_BILL
    val applyItemDiscount = isSplitBill || manualApplyItemDiscount

    // UNGROUP ITEMS FOR SPLIT BILL MODE (Qty > 1 expands into 1x individual items)
    val displayItems = remember(isSplitBill, items) {
        if (isSplitBill) {
            val expanded = mutableListOf<CheckoutPaymentItem>()
            items.forEach { item ->
                if (item.qty > 1) {
                    for (i in 1..item.qty) {
                        expanded.add(
                            CheckoutPaymentItem(
                                id = "${item.id}_sub_$i",
                                qty = 1,
                                name = item.name,
                                variant = item.variant,
                                unitPrice = item.unitPrice
                            )
                        )
                    }
                } else {
                    expanded.add(item)
                }
            }
            expanded
        } else {
            items
        }
    }

    // Filter items to calculate based on Split Bill vs Normal Mode
    val activeItemsForCalculation = remember(selectedBillMode, checkedSplitItems.toList(), displayItems) {
        if (isSplitBill) {
            displayItems.filter { checkedSplitItems.contains(it.id) && !paidSplitItemIds.contains(it.id) }
        } else {
            displayItems
        }
    }

    val isAnyItemCheckedInSplit = !isSplitBill || activeItemsForCalculation.isNotEmpty()

    // Calculation logic according to PAYMENT_LOGIC.md
    val rawSubtotal = activeItemsForCalculation.sumOf { it.totalOriginal }
    val itemDiscountTotal = activeItemsForCalculation.sumOf { item ->
        val pct = itemDiscountsPercent[item.id] ?: 0
        (item.totalOriginal * pct) / 100
    }
    
    // Potongan (global) - currently not hooked up to a UI state, assumed 0 for now as per view-only requirement
    val potongan = 0 
    
    val subtotalAfterDiscount = (rawSubtotal - itemDiscountTotal - potongan).coerceAtLeast(0)
    
    // Calculate per-item service and PPN based on PAYMENT_LOGIC.md
    val serviceTax = if (isAnyItemCheckedInSplit && rawSubtotal > 0) {
        activeItemsForCalculation.filter { it.hasService }.sumOf { item ->
            val pct = itemDiscountsPercent[item.id] ?: 0
            val itemDis = (item.totalOriginal * pct) / 100
            val itemSub = Math.max(0, item.totalOriginal - itemDis)
            Math.round(itemSub * 0.05).toInt()
        }
    } else 0
    
    val ppnTax = if (isAnyItemCheckedInSplit && rawSubtotal > 0) {
        activeItemsForCalculation.filter { it.hasPpn }.sumOf { item ->
            val pct = itemDiscountsPercent[item.id] ?: 0
            val itemDis = (item.totalOriginal * pct) / 100
            val itemSub = Math.max(0, item.totalOriginal - itemDis)
            val itemService = if (item.hasService) Math.round(itemSub * 0.05).toInt() else 0
            Math.round((itemSub + itemService) * 0.10).toInt()
        }
    } else 0
    
    val calculatedTotal = subtotalAfterDiscount + serviceTax + ppnTax
    
    // Pembulatan ke kelipatan 500
    val sisa = calculatedTotal % 1000
    val roundedTotal = when {
        !isAnyItemCheckedInSplit || calculatedTotal == 0 -> 0
        sisa < 250 -> calculatedTotal - sisa
        sisa < 750 -> calculatedTotal - sisa + 500
        else -> calculatedTotal - sisa + 1000
    }
    val pembulatan = if (roundedTotal > 0) roundedTotal - calculatedTotal else 0
    val totalToPay = roundedTotal

    // Auto-sync for non-cash payment methods only (QRIS/EDC/Transfer)
    LaunchedEffect(selectedPaymentMethod) {
        if (selectedPaymentMethod != PaymentMethod.TUNAI) {
            paidAmount = totalToPay
            customCashAmount = null
        }
    }

    val changeAmount = if (paidAmount >= totalToPay) paidAmount - totalToPay else 0

    // Custom Cash Dialog Popup
    if (showCustomCashDialog) {
        CustomCashInputDialog(
            initialValue = if (customCashAmount != null && customCashAmount!! > 0) customCashAmount.toString() else "",
            onConfirm = { amount ->
                paidAmount = amount
                customCashAmount = amount
                showCustomCashDialog = false
            },
            onDismiss = { showCustomCashDialog = false }
        )
    }

    // Payment Completion Success Dialog Popup
    activeCompletionResult?.let { result ->
        PaymentCompletedDialog(
            result = result,
            onContinueSplit = {
                activeCompletionResult = null
                paidAmount = 0
                customCashAmount = null
            },
            onPrintReceipt = {
                Toast.makeText(context, "Mencetak Struk Pembayaran...", Toast.LENGTH_SHORT).show()
                if (!result.hasUnpaidItems) {
                    onPaymentSuccess(paidAmount)
                    onDismiss()
                } else {
                    activeCompletionResult = null
                    paidAmount = 0
                    customCashAmount = null
                }
            },
            onFinishAndClose = {
                activeCompletionResult = null
                onPaymentSuccess(paidAmount)
                onDismiss()
            }
        )
    }

    Dialog(
        onDismissRequest = { /* Modal cannot close on outside click */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // SINGLE UNIFIED LARGE MODAL CARD WITH 2 PANELS SIDE-BY-SIDE
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .width(880.dp)
                    .height(570.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // ─── LEFT PANEL: TABLE INFO, TOGGLE & SCROLLABLE ITEM LIST ─────
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        // Header Table & Items count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Nomor Meja : ", fontSize = 13.sp, color = Color(0xFF475569))
                            Text(text = tableNo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "Jumlah Item : ", fontSize = 13.sp, color = Color(0xFF475569))
                            Text(text = displayItems.sumOf { it.qty }.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())

                        // Switch Diskon Per Item / Pilih Item Split Bill (RESETS DISCOUNTS ON TOGGLE OFF)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(
                                checked = applyItemDiscount,
                                onCheckedChange = { newValue ->
                                    if (!isSplitBill) {
                                        manualApplyItemDiscount = newValue
                                        if (!newValue) {
                                            itemDiscountsPercent.clear() // Reset all item discounts when turned OFF!
                                        }
                                    }
                                },
                                enabled = !isSplitBill,
                                modifier = Modifier.scale(0.75f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2563EB),
                                    disabledCheckedTrackColor = Color(0xFF2563EB).copy(alpha = 0.7f),
                                    disabledCheckedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCBD5E1)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSplitBill) "Pilih item yang akan dibayar" else "Terapkan Diskon Per Item",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        DashedDivider(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))

                        // SCROLLABLE ITEM LIST (ONLY ITEMS ARE SCROLLABLE!)
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(displayItems, key = { it.id }) { item ->
                                val isPaidAlready = isSplitBill && paidSplitItemIds.contains(item.id)
                                val isChecked = !isSplitBill || checkedSplitItems.contains(item.id) || isPaidAlready
                                val pct = itemDiscountsPercent[item.id] ?: 0
                                val discountNominal = (item.totalOriginal * pct) / 100

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isPaidAlready -> Color(0xFFF1F5F9)
                                                isSplitBill && isChecked -> Color(0xFFF0FDF4)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Custom Rounded Checkbox in Split Bill mode
                                        if (isSplitBill) {
                                            if (isPaidAlready) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFD1FAE5),
                                                    modifier = Modifier.padding(end = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "✓ Lunas",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF059669)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            } else {
                                                CustomRoundedCheckbox(
                                                    checked = isChecked,
                                                    onCheckedChange = {
                                                        if (checkedSplitItems.contains(item.id)) {
                                                            checkedSplitItems.remove(item.id)
                                                        } else {
                                                            checkedSplitItems.add(item.id)
                                                        }
                                                        paidAmount = 0
                                                        customCashAmount = null
                                                    },
                                                    enabled = !isPaidAlready,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isPaidAlready) Color(0xFFCBD5E1) else Color(0xFFDBEAFE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.qty.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPaidAlready) Color(0xFF64748B) else Color(0xFF2563EB),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 11.sp,
                                                style = TextStyle(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = item.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 15.sp,
                                                color = if (isPaidAlready) Color(0xFF94A3B8) else if (isChecked) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                                textDecoration = if (isPaidAlready) TextDecoration.LineThrough else TextDecoration.None,
                                                style = TextStyle(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                )
                                            )
                                            if (!item.variant.isNullOrBlank()) {
                                                Text(
                                                    text = "(${item.variant})",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    lineHeight = 13.sp,
                                                    color = if (isPaidAlready) Color(0xFFCBD5E1) else if (isChecked) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                    style = TextStyle(
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    modifier = Modifier.padding(top = 5.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            text = formatRupiah(item.totalOriginal),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPaidAlready) Color(0xFF94A3B8) else if (isChecked) Color(0xFF1E293B) else Color(0xFF94A3B8)
                                        )
                                    }

                                    // Per-item discount controls (Shown when applyItemDiscount is ON)
                                    AnimatedVisibility(
                                        visible = applyItemDiscount && !isPaidAlready,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isSplitBill) {
                                                Spacer(modifier = Modifier.width(30.dp))
                                            }

                                            // COMPACT STEPPER CONTROL WITH Snug HORIZONTAL PADDING
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Color(0xFFF1F5F9))
                                                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                // Minus Button (Clear contrast background & border)
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(if (pct > 0) Color(0xFFCBD5E1) else Color(0xFFE2E8F0))
                                                        .border(
                                                            BorderStroke(if (pct > 0) 1.dp else 0.dp, if (pct > 0) Color(0xFF94A3B8) else Color.Transparent),
                                                            CircleShape
                                                        )
                                                        .clickable(enabled = !isPaidAlready && pct > 0) {
                                                            itemDiscountsPercent[item.id] = (pct - 5).coerceIn(0, 100)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_lucide_minus),
                                                        contentDescription = "Kurang Diskon",
                                                        tint = if (pct > 0) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Text(
                                                    text = "$pct%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF059669)
                                                )

                                                Spacer(modifier = Modifier.width(4.dp))

                                                // Plus Button (Solid Green with White Icon)
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF059669))
                                                        .clickable(enabled = !isPaidAlready) {
                                                            itemDiscountsPercent[item.id] = (pct + 5).coerceIn(0, 100)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_lucide_plus),
                                                        contentDescription = "Tambah Diskon",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            // Nominal Tag Badge
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_lucide_tag),
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = formatRupiah(discountNominal),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }

                                DashedDivider(color = Color(0xFFF1F5F9), modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    // Vertical Separator Line between Left and Right Panels
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // ─── RIGHT PANEL: INVOICE HEADER, CALCULATION SUMMARY & CONTROLS ─
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Invoice + Close Button (The ONLY way to close modal!)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (invoiceNo.startsWith("#")) invoiceNo else "#$invoiceNo",
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_x),
                                    contentDescription = "Tutup",
                                    tint = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))

                        // SUMMARY BREAKDOWN
                        PaymentRow(label = "Subtotal :", value = formatRupiah(rawSubtotal))
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(
                            label = "Potongan :",
                            value = formatRupiah(potongan),
                            valueColor = Color(0xFF1E293B),
                            isDashedUnderline = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(label = "Diskon Item :", value = formatRupiah(itemDiscountTotal))
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(label = "Service (5%) :", value = formatRupiah(serviceTax))
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(label = "PPN (10%) :", value = formatRupiah(ppnTax))
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(label = "Total :", value = formatRupiah(calculatedTotal), isBold = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(label = "Pembulatan :", value = formatRupiah(pembulatan))
                        Spacer(modifier = Modifier.height(6.dp))
                        PaymentRow(
                            label = "Total yg Harus dibayar :",
                            value = formatRupiah(totalToPay),
                            isExtraBold = true,
                            fontSize = 15.sp,
                            valueColor = Color(0xFF2563EB)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(14.dp))

                        // BILL MODE PILLS (NORMAL | SPLIT BILL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BillMode.entries.forEach { mode ->
                                val isSelected = mode == selectedBillMode
                                PillOptionButton(
                                    text = mode.label,
                                    iconRes = mode.iconRes,
                                    isSelected = isSelected,
                                    activeContainerColor = Color(0xFFDBEAFE),
                                    activeContentColor = Color(0xFF2563EB),
                                    inactiveContainerColor = Color(0xFFEFF6FF),
                                    inactiveContentColor = Color(0xFF2563EB).copy(alpha = 0.8f),
                                    onClick = {
                                        selectedBillMode = mode
                                        paidAmount = 0
                                        customCashAmount = null
                                        if (mode == BillMode.SPLIT_BILL && checkedSplitItems.isEmpty()) {
                                            displayItems.firstOrNull { !paidSplitItemIds.contains(it.id) }?.let { checkedSplitItems.add(it.id) }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // PAYMENT METHOD PILLS (TUNAI | QRIS | EDC | TRANSFER)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                val isSelected = method == selectedPaymentMethod
                                PillOptionButton(
                                    text = method.label,
                                    iconRes = method.iconRes,
                                    isSelected = isSelected,
                                    enabled = isAnyItemCheckedInSplit && totalToPay > 0,
                                    activeContainerColor = Color(0xFFD1FAE5),
                                    activeContentColor = Color(0xFF059669),
                                    inactiveContainerColor = Color(0xFFECFDF5).copy(alpha = 0.5f),
                                    inactiveContentColor = Color(0xFF059669).copy(alpha = 0.8f),
                                    onClick = {
                                        selectedPaymentMethod = method
                                        if (method != PaymentMethod.TUNAI) {
                                            paidAmount = totalToPay
                                            customCashAmount = null
                                        } else {
                                            paidAmount = 0
                                            customCashAmount = null
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))

                        // QUICK CASH SUGGESTIONS (SMART ASCENDING ALGORITHM + LAINNYA)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presets = remember(totalToPay) {
                                generateSmartCashSuggestions(totalToPay)
                            }

                            val isCustomActive = customCashAmount != null && paidAmount == customCashAmount

                            presets.forEach { (label, amount) ->
                                // Uang pas & cash options are NOT checked by default! Only checked when clicked!
                                val isSelected = paidAmount == amount && !isCustomActive && paidAmount > 0 && isAnyItemCheckedInSplit && totalToPay > 0
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFF06B6D4) else Color.White)
                                        .border(BorderStroke(1.dp, if (isSelected) Color(0xFF06B6D4) else Color(0xFF22D3EE)), RoundedCornerShape(20.dp))
                                        .clickable(enabled = isAnyItemCheckedInSplit && totalToPay > 0) {
                                            paidAmount = amount
                                            customCashAmount = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else if (isAnyItemCheckedInSplit && totalToPay > 0) Color(0xFF0891B2) else Color.Gray
                                    )
                                }
                            }

                            // "Lainnya" Pill Button
                            val customLabel = if (isCustomActive && customCashAmount != null) {
                                formatNominalShort(customCashAmount!!)
                            } else {
                                "Lainnya"
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isCustomActive) Color(0xFF06B6D4) else Color.White)
                                    .border(BorderStroke(1.dp, if (isCustomActive) Color(0xFF06B6D4) else Color(0xFF22D3EE)), RoundedCornerShape(20.dp))
                                    .clickable(enabled = isAnyItemCheckedInSplit && totalToPay > 0) { showCustomCashDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCustomActive) Color.White else if (isAnyItemCheckedInSplit && totalToPay > 0) Color(0xFF0891B2) else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(14.dp))

                        // CONFIRM PAYMENT BUTTON (NOMINAL APPEARS ONLY WHEN PAYMENT METHOD / CASH SUGGESTION IS CLICKED!)
                        val isPaymentReady = isAnyItemCheckedInSplit && totalToPay > 0 && paidAmount > 0

                        val confirmText = remember(selectedPaymentMethod, totalToPay, paidAmount, isPaymentReady) {
                            if (isPaymentReady) {
                                if (selectedPaymentMethod == PaymentMethod.TUNAI && paidAmount > totalToPay) {
                                    "Konfirmasi Pembayaran (${formatRupiah(totalToPay)})"
                                } else {
                                    "Konfirmasi Pembayaran • ${formatRupiah(totalToPay)}"
                                }
                            } else {
                                "Konfirmasi Pembayaran"
                            }
                        }

                        Button(
                            onClick = {
                                if (!isAnyItemCheckedInSplit || totalToPay <= 0) {
                                    Toast.makeText(context, "Pilih setidaknya 1 item yang belum dibayar!", Toast.LENGTH_SHORT).show()
                                } else if (paidAmount <= 0) {
                                    Toast.makeText(context, "Pilih nominal pembayaran terlebih dahulu!", Toast.LENGTH_SHORT).show()
                                } else if (paidAmount < totalToPay && selectedPaymentMethod == PaymentMethod.TUNAI) {
                                    Toast.makeText(context, "Jumlah pembayaran kurang dari total tagihan!", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (isSplitBill) {
                                        // Mark paid items
                                        paidSplitItemIds.addAll(checkedSplitItems)
                                        val remainingUnpaid = displayItems.any { !paidSplitItemIds.contains(it.id) }
                                        val result = CompletedPaymentResult(
                                            invoiceNo = invoiceNo,
                                            tableNo = tableNo,
                                            changeAmount = changeAmount,
                                            isSplitBill = true,
                                            hasUnpaidItems = remainingUnpaid
                                        )
                                        checkedSplitItems.clear()
                                        // Auto check next unpaid item if any
                                        displayItems.firstOrNull { !paidSplitItemIds.contains(it.id) }?.let { checkedSplitItems.add(it.id) }
                                        activeCompletionResult = result
                                    } else {
                                        val result = CompletedPaymentResult(
                                            invoiceNo = invoiceNo,
                                            tableNo = tableNo,
                                            changeAmount = changeAmount,
                                            isSplitBill = false,
                                            hasUnpaidItems = false
                                        )
                                        activeCompletionResult = result
                                    }
                                }
                            },
                            enabled = isPaymentReady,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFCBD5E1),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_check),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = confirmText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── CUSTOM ROUNDED CHECKBOX FOR SPLIT BILL ITEMS ────────────────────────────
@Composable
private fun CustomRoundedCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (checked) Color(0xFF059669) else Color.White)
            .border(
                BorderStroke(
                    if (checked) 0.dp else 1.5.dp,
                    if (checked) Color.Transparent else Color(0xFF94A3B8)
                ),
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

// ─── PAYMENT COMPLETED SUCCESS POPUP DIALOG ──────────────────────────────────
@Composable
private fun PaymentCompletedDialog(
    result: CompletedPaymentResult,
    onContinueSplit: () -> Unit,
    onPrintReceipt: () -> Unit,
    onFinishAndClose: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Modal completion popup cannot close on outside click */ },
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(340.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_check),
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pembayaran Berhasil!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "#${result.invoiceNo} • Meja ${result.tableNo}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                // Display Change Amount ONLY if change > 0!
                if (result.changeAmount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFECFDF5))
                            .border(BorderStroke(1.dp, Color(0xFFA7F3D0)), RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Kembalian",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = formatRupiah(result.changeAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                DashedDivider(color = Color(0xFFCBD5E1), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (result.isSplitBill && result.hasUnpaidItems) {
                        // Split Bill with remaining unpaid items: "Lanjutkan Pembayaran" | "Cetak Struk"
                        Button(
                            onClick = onContinueSplit,
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF334155)
                            ),
                            elevation = null
                        ) {
                            Text(
                                text = "Lanjutkan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Normal mode OR Split Bill with all items completed: "Tutup" | "Cetak Struk"
                        Button(
                            onClick = onFinishAndClose,
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tutup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Button(
                        onClick = onPrintReceipt,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        elevation = null
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_printer),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cetak Struk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── CUSTOM CASH INPUT POPUP DIALOG ──────────────────────────────────────────
@Composable
private fun CustomCashInputDialog(
    initialValue: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf(initialValue) }
    val parsedAmount = inputText.toIntOrNull() ?: 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(320.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Input Nominal Uang Bayar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        inputText = digits
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                if (parsedAmount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatRupiah(parsedAmount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0891B2)
                    )
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
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF64748B),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Batal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onConfirm(parsedAmount) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06B6D4),
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

@Composable
private fun PillOptionButton(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    enabled: Boolean = true,
    activeContainerColor: Color,
    activeContentColor: Color,
    inactiveContainerColor: Color,
    inactiveContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (!enabled) Color(0xFFF1F5F9) else if (isSelected) activeContainerColor else inactiveContainerColor,
        border = if (isSelected && enabled) BorderStroke(1.dp, activeContentColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected && enabled) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(activeContentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_check),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (enabled) inactiveContentColor else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!enabled) Color.Gray else if (isSelected) activeContentColor else inactiveContentColor
            )
        }
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isExtraBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    valueColor: Color = Color(0xFF1E293B),
    isDashedUnderline: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = if (isExtraBold) fontSize else 12.5.sp,
            fontWeight = if (isExtraBold) FontWeight.ExtraBold else if (isBold) FontWeight.Bold else FontWeight.Bold,
            color = valueColor,
            textDecoration = if (isDashedUnderline) TextDecoration.Underline else TextDecoration.None
        )
    }
}

private fun generateSmartCashSuggestions(totalToPay: Int): List<Pair<String, Int>> {
    if (totalToPay <= 0) {
        return listOf(
            "Uang Pas" to 0,
            "50.000" to 50_000,
            "75.000" to 75_000,
            "100.000" to 100_000
        )
    }

    val candidates = mutableSetOf<Int>()

    // Standard steps based on Indonesian Banknote denominations
    val s1 = ((totalToPay / 5000) + 1) * 5000
    val s2 = ((totalToPay / 10000) + 1) * 10000
    val s3 = ((totalToPay / 20000) + 1) * 20000
    val s4 = ((totalToPay / 50000) + 1) * 50000
    val s5 = if (totalToPay < 50000) 70000 else if (totalToPay < 80000) 80000 else 120000
    val s6 = ((totalToPay / 100000) + 1) * 100000

    listOf(s1, s2, s3, s4, s5, s6).forEach { amount ->
        if (amount > totalToPay) {
            candidates.add(amount)
        }
    }

    val sortedAmounts = candidates.sorted()

    val result = mutableListOf<Pair<String, Int>>()
    result.add("Uang Pas" to totalToPay)

    sortedAmounts.forEach { amount ->
        if (result.size < 4) {
            result.add(formatNominalShort(amount) to amount)
        }
    }

    while (result.size < 4) {
        val lastVal = result.last().second
        val nextVal = lastVal + 20_000
        result.add(formatNominalShort(nextVal) to nextVal)
    }

    return result.take(4)
}

private fun formatRupiah(value: Int): String {
    return "Rp ${"%,d".format(value).replace(",", ".")}"
}

private fun formatNominalShort(value: Int): String {
    return if (value >= 1000 && value % 1000 == 0) {
        "${value / 1000}.000"
    } else {
        "%,d".format(value).replace(",", ".")
    }
}
