package hadi.veri.kokababystore.data.repository

import hadi.veri.kokababystore.data.Order
import hadi.veri.kokababystore.data.Product
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun getProductsFlow(): Flow<List<Product>>
    suspend fun getProduct(productId: String): Product?
    suspend fun saveProduct(product: Product)
    suspend fun deleteProduct(productId: String, imageUrl: String?)
    suspend fun uploadProductImage(imageBytes: ByteArray, fileName: String): String
    suspend fun seedInitialData()
    fun getOrders(): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, newStatus: String) // Added this
    suspend fun updateProductStock(productId: String, quantityChange: Int)
}
