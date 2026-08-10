package hadi.veri.kokababystore.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import hadi.veri.kokababystore.data.repository.AdminRepository
import hadi.veri.kokababystore.data.service.AuthService
import hadi.veri.kokababystore.data.service.OrderService

class AdminViewModelFactory(
    private val adminRepository: AdminRepository,
    private val orderService: OrderService,
    private val authService: AuthService,
    private val firestore: FirebaseFirestore // Added Firestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(adminRepository, orderService, authService, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
