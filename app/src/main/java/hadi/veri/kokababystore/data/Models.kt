package hadi.veri.kokababystore.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val email: String? = null,
    val role: String = "customer",
    val fullName: String? = null,
    val addresses: List<String> = emptyList()
)

data class Product(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val stock: Int = 0,
    val imageUrl: String = "",
    val description: String = "",
    val price: Double = 0.0
)

// Data class for Shopping Cart Item
data class CartItem(
    @DocumentId
    val id: String = "", // Cart item's own ID
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0,
    var quantity: Int = 1
)

data class Order(
    @DocumentId
    val id: String = "",
    val customerId: String = "",
    val orderDate: Long = 0L,
    val totalAmount: Double = 0.0,
    val shippingAddress: String = "",
    val status: String = "pending", // e.g., pending, processing, shipped, delivered, cancelled
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

data class OrderAlert(
    val totalPending: Long = 0L
)
