package hadi.veri.kokababystore.ui.customer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import hadi.veri.kokababystore.data.CartItem
import hadi.veri.kokababystore.data.Order
import hadi.veri.kokababystore.data.OrderItem
import hadi.veri.kokababystore.data.Product
import hadi.veri.kokababystore.data.repository.AdminRepository
import hadi.veri.kokababystore.data.repository.CustomerRepository
import hadi.veri.kokababystore.data.service.AuthService
import hadi.veri.kokababystore.data.service.OrderService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

class CustomerViewModel(
    private val adminRepository: AdminRepository,
    private val customerRepository: CustomerRepository,
    private val authService: AuthService,
    private val orderService: OrderService // Added OrderService
) : ViewModel() {

    private val _orderPlacementState = MutableStateFlow<OrderPlacementState>(OrderPlacementState.Idle)
    val orderPlacementState: StateFlow<OrderPlacementState> = _orderPlacementState.asStateFlow()

    val products: StateFlow<List<Product>> = adminRepository.getProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Safely handle flows that depend on the user being logged in
    val cartItems: StateFlow<List<CartItem>> = authService.authState
        .flatMapLatest { user ->
            if (user != null) {
                customerRepository.getCartItems(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = authService.authState
        .flatMapLatest { user ->
            if (user != null) {
                customerRepository.getOrdersForCustomer(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = cartItems.map { it.sumOf { item -> item.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onAddToCart(product: Product, quantity: Int) {
        if (quantity <= 0) return
        authService.currentUser?.uid?.let { id ->
            viewModelScope.launch {
                customerRepository.addToCart(id, product, quantity)
            }
        }
    }

    fun onUpdateQuantity(cartItemId: String, newQuantity: Int) {
        viewModelScope.launch {
            customerRepository.updateCartItemQuantity(cartItemId, newQuantity)
        }
    }

    fun onRemoveItem(cartItemId: String) {
        viewModelScope.launch {
            customerRepository.removeCartItem(cartItemId)
        }
    }

    fun placeOrder(shippingAddress: String) {
        if (_orderPlacementState.value == OrderPlacementState.Placing) return

        authService.currentUser?.uid?.let { id ->
            viewModelScope.launch {
                _orderPlacementState.value = OrderPlacementState.Placing
                try {
                    // Force a timeout if the whole process takes more than 10 seconds
                    withTimeout(10_000L) {
                        val itemsInCart = cartItems.first()
                        if (itemsInCart.isEmpty()) {
                            _orderPlacementState.value = OrderPlacementState.Error("Cart is empty")
                            return@withTimeout
                        }

                        val orderId = UUID.randomUUID().toString()
                        val totalAmount = itemsInCart.sumOf { it.productPrice * it.quantity }

                        val order = Order(
                            id = orderId,
                            customerId = id,
                            orderDate = System.currentTimeMillis(),
                            shippingAddress = shippingAddress,
                            status = "pending",
                            items = itemsInCart.map {
                                OrderItem(
                                    productId = it.productId,
                                    name = it.productName,
                                    quantity = it.quantity,
                                    price = it.productPrice
                                )
                            },
                            totalAmount = totalAmount
                        )

                        for (item in order.items) {
                            adminRepository.updateProductStock(item.productId, -item.quantity)
                        }

                        customerRepository.placeOrder(order)
                        customerRepository.clearCart(id)

                        orderService.incrementPendingOrderCount()
                        orderService.sendNewOrderNotification(orderId, totalAmount)

                        _orderPlacementState.value = OrderPlacementState.Success
                    }
                } catch (e: Exception) {
                    Log.e("CustomerViewModel", "Order placement failed or timed out", e)
                    _orderPlacementState.value = OrderPlacementState.Error(e.message ?: "An error occurred")
                }
            }
        }
    }

    fun resetOrderPlacementState() {
        _orderPlacementState.value = OrderPlacementState.Idle
    }
    
    fun logout() {
        viewModelScope.launch {
            authService.logout()
        }
    }
}

sealed class OrderPlacementState {
    object Idle : OrderPlacementState()
    object Placing : OrderPlacementState()
    object Success : OrderPlacementState()
    data class Error(val message: String) : OrderPlacementState()
}


class CustomerViewModelFactory(
    private val adminRepository: AdminRepository,
    private val customerRepository: CustomerRepository,
    private val authService: AuthService,
    private val orderService: OrderService // Added OrderService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomerViewModel(adminRepository, customerRepository, authService, orderService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
