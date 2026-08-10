package hadi.veri.kokababystore.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import hadi.veri.kokababystore.data.CartItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CustomerViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    var shippingAddress by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val orderState by viewModel.orderPlacementState.collectAsState()

    LaunchedEffect(orderState) {
        when (orderState) {
            is OrderPlacementState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Order placed successfully!")
                }
                navController.popBackStack() // Navigate back safely
                viewModel.resetOrderPlacementState() // Reset the state
            }
            is OrderPlacementState.Error -> {
                val errorMessage = (orderState as OrderPlacementState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar("Error: $errorMessage")
                }
                viewModel.resetOrderPlacementState()
            }
            else -> Unit // Do nothing for Idle or Placing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("My Cart & Checkout") })
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        val total = cartItems.sumOf { it.productPrice * it.quantity }
                        Text(
                            text = "Total: Rp $total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = shippingAddress,
                            onValueChange = { shippingAddress = it },
                            label = { Text("Shipping Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (shippingAddress.isNotBlank()) {
                                    viewModel.placeOrder(shippingAddress)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please enter a shipping address.")
                                    }
                                }
                            },
                            enabled = orderState != OrderPlacementState.Placing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (orderState == OrderPlacementState.Placing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("PLACE ORDER")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Your cart is empty.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item = item, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, viewModel: CustomerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Rp ${item.productPrice}")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.onUpdateQuantity(item.id, item.quantity - 1) }) {
                Icon(Icons.Default.Remove, "Decrease Quantity")
            }
            Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = { viewModel.onUpdateQuantity(item.id, item.quantity + 1) }) {
                Icon(Icons.Default.Add, "Increase Quantity")
            }
        }
    }
}
