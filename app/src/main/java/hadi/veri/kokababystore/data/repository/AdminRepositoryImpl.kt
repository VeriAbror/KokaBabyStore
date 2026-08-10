package hadi.veri.kokababystore.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import hadi.veri.kokababystore.data.Order
import hadi.veri.kokababystore.data.Product
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: Storage
) : AdminRepository {

    private val productsCollection = firestore.collection("products")
    private val ordersCollection = firestore.collection("orders")
    private val productsBucket = "product_images" // Corrected bucket name

    override fun getProductsFlow(): Flow<List<Product>> {
        return productsCollection.snapshots().map { snapshot ->
            snapshot.toObjects(Product::class.java)
        }
    }

    override suspend fun getProduct(productId: String): Product? {
        return try {
            productsCollection.document(productId).get().await().toObject<Product>()
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error getting product $productId", e)
            null
        }
    }

    override fun getOrders(): Flow<List<Order>> {
        return ordersCollection.orderBy("orderDate", Query.Direction.DESCENDING).snapshots().map {
            it.toObjects(Order::class.java)
        }
    }

    override suspend fun saveProduct(product: Product) {
        productsCollection.document(product.id).set(product).await()
    }

    override suspend fun deleteProduct(productId: String, imageUrl: String?) {
        try {
            imageUrl?.let { url ->
                if (url.isNotBlank() && url.contains(productsBucket)) {
                    val path = url.substringAfterLast("/")
                    storage[productsBucket].delete(listOf(path))
                    Log.d("AdminRepository", "Image $path deleted from Supabase.")
                }
            }
            productsCollection.document(productId).delete().await()
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error deleting product $productId", e)
            throw e
        }
    }

    override suspend fun uploadProductImage(imageBytes: ByteArray, fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                storage[productsBucket].upload(fileName, imageBytes, upsert = true)
                storage[productsBucket].publicUrl(fileName)
            } catch (e: Exception) {
                Log.e("AdminRepository", "Error uploading image to Supabase", e)
                ""
            }
        }
    }

    override suspend fun seedInitialData() {
        // This function is now intentionally left empty.
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        ordersCollection.document(orderId).update("status", newStatus).await()
    }

    override suspend fun updateProductStock(productId: String, quantityChange: Int) {
        val productRef = productsCollection.document(productId)
        firestore.runTransaction {
            val snapshot = it.get(productRef)
            val currentStock = snapshot.getLong("stock") ?: 0
            val newStock = currentStock + quantityChange
            it.update(productRef, "stock", newStock)
        }.await()
    }
}
