package hadi.veri.kokababystore.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun AdminOrderScreen(
    navController: NavController,
    viewModel: AdminViewModel
) {
    val orders by viewModel.orders.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Customer Orders") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                OrderCard(order = order, onStatusChange = {
                    viewModel.updateOrderStatus(order, it)
                })
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onStatusChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val possibleStatus = listOf("pending", "processing", "shipped", "delivered", "cancelled")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Order #${order.id.take(6).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Change Status")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        possibleStatus.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                onClick = { 
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Text(
                text = "Customer: ${order.customerId.take(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total: Rp ${order.totalAmount}")
            Text("Status: ${order.status.replaceFirstChar { it.uppercase() }}")
            Text("Date: ${order.orderDate.toDateString()}")
        }
    }
}

private fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}
