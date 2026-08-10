package hadi.veri.kokababystore.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import hadi.veri.kokababystore.data.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl(private val firestore: FirebaseFirestore) : ProductRepository {
    override fun getProducts(): Flow<List<Product>> = flow {
        val snapshot = firestore.collection("products").get().await()
        val products = snapshot.toObjects(Product::class.java)
        emit(products)
    }
}
