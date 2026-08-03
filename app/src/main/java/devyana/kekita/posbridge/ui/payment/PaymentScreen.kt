package devyana.kekita.posbridge.ui.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import devyana.kekita.posbridge.R
import devyana.kekita.posbridge.ui.components.DashedDivider
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentItem
import devyana.kekita.posbridge.ui.payment.components.CheckoutPaymentModal

data class PaymentTransactionOrder(
    val invoiceNo: String,
    val tableNo: String,
    val itemCount: Int,
    val totalAmount: Int,
    val isPaid: Boolean,
    val items: List<CheckoutPaymentItem>
)

@Composable
fun PaymentScreenContent() {
    val colorScheme = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(0) } // 0 = Belum Dibayar, 1 = Sudah Dibayar
    var selectedOrderForCheckout by remember { mutableStateOf<PaymentTransactionOrder?>(null) }

    val sampleOrders = remember {
        listOf(
            PaymentTransactionOrder(
                invoiceNo = "Invoice #260803001",
                tableNo = "10",
                itemCount = 4,
                totalAmount = 73_000,
                isPaid = false,
                items = listOf(
                    CheckoutPaymentItem("1", 1, "Soto Ayam", null, 35_000),
                    CheckoutPaymentItem("2", 1, "Steam rice", null, 8_000),
                    CheckoutPaymentItem("3", 1, "Aqua 600ml", null, 6_000),
                    CheckoutPaymentItem("4", 1, "Eggs", "OMELETE", 15_000)
                )
            ),
            PaymentTransactionOrder(
                invoiceNo = "Invoice #260803002",
                tableNo = "04",
                itemCount = 2,
                totalAmount = 45_000,
                isPaid = false,
                items = listOf(
                    CheckoutPaymentItem("1", 1, "Special Fried Rice", null, 40_000),
                    CheckoutPaymentItem("2", 1, "Original Tea", "ICE", 10_000)
                )
            ),
            PaymentTransactionOrder(
                invoiceNo = "Invoice #260803003",
                tableNo = "02",
                itemCount = 3,
                totalAmount = 94_000,
                isPaid = false,
                items = listOf(
                    CheckoutPaymentItem("1", 2, "Ayam Bakar", null, 50_000),
                    CheckoutPaymentItem("2", 1, "Cappucino", "HOT", 30_000),
                    CheckoutPaymentItem("3", 2, "Steam rice", null, 8_000),
                    CheckoutPaymentItem("4", 1, "Aqua 600ml", null, 6_000)
                )
            ),
            PaymentTransactionOrder(
                invoiceNo = "Invoice #260803000",
                tableNo = "02",
                itemCount = 3,
                totalAmount = 85_000,
                isPaid = true,
                items = listOf(
                    CheckoutPaymentItem("1", 2, "Ayam Bakar", null, 50_000),
                    CheckoutPaymentItem("2", 1, "Cappucino", "HOT", 30_000),
                    CheckoutPaymentItem("3", 2, "Steam rice", null, 5_000)
                )
            )
        )
    }

    val filteredOrders = remember(selectedTab, sampleOrders) {
        if (selectedTab == 0) sampleOrders.filter { !it.isPaid }
        else sampleOrders.filter { it.isPaid }
    }

    // Modal Pembayaran Checkout jika ada transaksi yang dipilih
    selectedOrderForCheckout?.let { order ->
        CheckoutPaymentModal(
            invoiceNo = order.invoiceNo,
            tableNo = order.tableNo,
            items = order.items,
            onDismiss = { selectedOrderForCheckout = null },
            onPaymentSuccess = { selectedOrderForCheckout = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💳 Status Pembayaran",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs Selection (Belum Dibayar & Sudah Dibayar)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val unpaidCount = sampleOrders.count { !it.isPaid }
            val paidCount = sampleOrders.count { it.isPaid }

            PaymentTabChip(
                text = "Belum Dibayar ($unpaidCount)",
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            PaymentTabChip(
                text = "Sudah Dibayar ($paidCount)",
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders, key = { it.invoiceNo }) { order ->
                    PaymentOrderCard(
                        order = order,
                        onPayClick = { selectedOrderForCheckout = order }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentTabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
        contentColor = if (selected) Color.White else Color(0xFF475569)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PaymentOrderCard(
    order: PaymentTransactionOrder,
    onPayClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = order.invoiceNo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (order.isPaid) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (order.isPaid) "Lunas" else "Belum Dibayar",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isPaid) Color(0xFF059669) else Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            DashedDivider(color = Color(0xFFF1F5F9), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Nomor Meja: ${order.tableNo}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(text = "${order.itemCount} Item Pesanan", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Tagihan", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                    Text(
                        text = formatRupiah(order.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                }
            }

            if (!order.isPaid) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_credit_card),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Bayar Sekarang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatRupiah(value: Int): String {
    return "Rp ${"%,d".format(value).replace(",", ".")}"
}
