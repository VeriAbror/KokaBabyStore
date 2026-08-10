package hadi.veri.kokababystore.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import hadi.veri.kokababystore.data.Order
import hadi.veri.kokababystore.data.OrderAlert
import hadi.veri.kokababystore.data.Product
import hadi.veri.kokababystore.data.repository.AdminRepository
import hadi.veri.kokababystore.data.service.AuthService
import hadi.veri.kokababystore.data.service.OrderService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val orderService: OrderService,
    private val authService: AuthService,
    private val firestore: FirebaseFirestore // Keep for direct queries
) : ViewModel() {

    val products: Flow<List<Product>> = adminRepository.getProductsFlow()
    val orders: Flow<List<Order>> = adminRepository.getOrders()

    val orderAlerts: StateFlow<OrderAlert> = orderService.listenToOrderAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderAlert())

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    fun loadProduct(productId: String?) {
        if (productId.isNullOrEmpty() || productId == "null") {
            _product.value = Product() // New product form
            return
        }
        viewModelScope.launch {
            _product.value = null // Signal loading
            _product.value = adminRepository.getProduct(productId)
        }
    }

    fun clearProductState() {
        _product.value = null
        _uiState.value = UiState.Idle
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            try {
                val oldStatus = order.status

                if (oldStatus.equals(newStatus, ignoreCase = true)) return@launch
                if (oldStatus.equals("delivered", ignoreCase = true) || oldStatus.equals("cancelled", ignoreCase = true)) {
                    return@launch
                }

                adminRepository.updateOrderStatus(order.id, newStatus)

                // --- LOGIKA STOK BARU ---
                // Hanya kembalikan stok jika status sebelumnya BUKAN 'delivered' atau 'cancelled'
                if (newStatus.equals("cancelled", ignoreCase = true)) {
                    for (item in order.items) {
                        adminRepository.updateProductStock(item.productId, item.quantity) // Kembalikan stok
                    }
                    Log.d("AdminViewModel", "Stock restored for cancelled order ${order.id}")
                }

                // --- LOGIKA COUNTER ---
                if (oldStatus.equals("pending", ignoreCase = true) && !newStatus.equals("pending", ignoreCase = true)) {
                    orderService.decrementPendingOrderCount()
                }

                // --- LOGIKA NOTIFIKASI ---
                val userDoc = firestore.collection("users").document(order.customerId).get().await()
                val fcmToken = userDoc.getString("fcmToken")
                if (!fcmToken.isNullOrBlank()) {
                    orderService.sendOrderStatusUpdateNotification(fcmToken, order.id, newStatus)
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update status")
            }
        }
    }


    fun saveProduct(product: Product, imageBytes: ByteArray?) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val productToSave = if (product.id.isEmpty()) {
                    product.copy(id = UUID.randomUUID().toString())
                } else {
                    product
                }

                val imageUrl = if (imageBytes != null) {
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val uploadedUrl = adminRepository.uploadProductImage(imageBytes, fileName)
                    if (uploadedUrl.isBlank()) {
                        throw Exception("Image upload failed.")
                    }
                    uploadedUrl
                } else {
                    productToSave.imageUrl
                }

                if (imageUrl.isBlank()) {
                    throw Exception("An image is required.")
                }

                adminRepository.saveProduct(productToSave.copy(imageUrl = imageUrl))
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                adminRepository.deleteProduct(product.id, product.imageUrl)
            } catch (e: Exception) {
                 _uiState.value = UiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
        }
    }

    fun resetState(){
        _uiState.value = UiState.Idle
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}
