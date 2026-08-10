package hadi.veri.kokababystore.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import hadi.veri.kokababystore.data.Order
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: CustomerViewModel
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Orders") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (orders.isEmpty()) {
                item {
                    Text("You have no orders yet.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(orders) { order ->
                    OrderHistoryCard(order)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order #${order.id.take(6).uppercase()}", fontWeight = FontWeight.Bold)
            Text("Date: ${order.orderDate.toDateString()}")
            Text("Total: Rp ${order.totalAmount}")
            Text("Status: ${order.status.replaceFirstChar { it.uppercase() }}", color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            order.items.forEach {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("${it.name} (x${it.quantity})", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}
