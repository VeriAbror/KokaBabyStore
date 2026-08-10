package hadi.veri.kokababystore.data.repository

import hadi.veri.kokababystore.data.CartItem
import hadi.veri.kokababystore.data.Order
import hadi.veri.kokababystore.data.Product
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCartItems(userId: String): Flow<List<CartItem>>
    fun getOrdersForCustomer(userId: String): Flow<List<Order>>
    suspend fun addToCart(userId: String, product: Product, quantity: Int) // Added quantity
    suspend fun updateCartItemQuantity(cartItemId: String, newQuantity: Int)
    suspend fun removeCartItem(cartItemId: String)
    suspend fun clearCart(userId: String)
    suspend fun placeOrder(order: Order)
}
