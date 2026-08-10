package hadi.veri.kokababystore.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import hadi.veri.kokababystore.data.Product
import hadi.veri.kokababystore.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    viewModel: CustomerViewModel
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = productId) {
        scope.launch {
            // We need a way to get a single product, let's assume it's in the viewmodel
            // For now, let's find it from the list of all products
            product = viewModel.products.value.find { it.id == productId }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(product?.name ?: "Product Detail") }) },
        content = {
            if (product == null) {
                Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(it).padding(16.dp)
                ) {
                    AsyncImage(
                        model = product!!.imageUrl,
                        contentDescription = product!!.name,
                        modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(product!!.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Rp ${product!!.price}", fontSize = 20.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Stock: ${product!!.stock}", fontSize = 16.sp, color = if (product!!.stock > 0) Color.Unspecified else Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(product!!.description)
                    Spacer(modifier = Modifier.weight(1f))

                    if (product!!.stock > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                Icon(Icons.Default.Remove, "Decrease quantity")
                            }
                            Text("$quantity", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 16.dp))
                            IconButton(onClick = { if (quantity < product!!.stock) quantity++ }) {
                                Icon(Icons.Default.Add, "Increase quantity")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            viewModel.onAddToCart(product!!, quantity)
                            navController.popBackStack() // Go back after adding to cart
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = product!!.stock > 0
                    ) {
                        Text(
                            text = if (product!!.stock > 0) "Add to Cart" else "Out of Stock",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    )
}
