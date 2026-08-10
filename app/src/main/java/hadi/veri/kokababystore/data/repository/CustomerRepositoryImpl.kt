package hadi.veri.kokababystore.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hadi.veri.kokababystore.data.CartItem
import hadi.veri.kokababystore.data.Order
import hadi.veri.kokababystore.data.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CustomerRepositoryImpl(private val firestore: FirebaseFirestore) : CustomerRepository {

    private val cartCollection = firestore.collection("carts")
    private val ordersCollection = firestore.collection("orders")

    override fun getCartItems(userId: String): Flow<List<CartItem>> = callbackFlow {
        val listener = cartCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("CustomerRepo", "Listen failed.", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val items = snapshots.toObjects(CartItem::class.java)
                    trySend(items).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getOrdersForCustomer(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection.whereEqualTo("customerId", userId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("CustomerRepo", "Listen failed.", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val orders = snapshots.toObjects(Order::class.java)
                    trySend(orders).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }


    override suspend fun addToCart(userId: String, product: Product, quantity: Int) {
        try {
            val query = cartCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", product.id)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                // If not in cart, add as a new item with the specified quantity
                val cartItemId = cartCollection.document().id
                val newItem = CartItem(
                    id = cartItemId,
                    userId = userId,
                    productId = product.id,
                    productName = product.name,
                    productImage = product.imageUrl,
                    productPrice = product.price,
                    quantity = quantity
                )
                cartCollection.document(cartItemId).set(newItem).await()
            } else {
                // If already in cart, increment by the specified quantity
                val docId = query.documents.first().id
                cartCollection.document(docId).update("quantity", FieldValue.increment(quantity.toLong())).await()
            }
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error adding to cart", e)
        }
    }

    override suspend fun updateCartItemQuantity(cartItemId: String, newQuantity: Int) {
        if (newQuantity > 0) {
            cartCollection.document(cartItemId).update("quantity", newQuantity).await()
        } else {
            removeCartItem(cartItemId)
        }
    }

    override suspend fun removeCartItem(cartItemId: String) {
        cartCollection.document(cartItemId).delete().await()
    }

    override suspend fun clearCart(userId: String) {
        try {
            val querySnapshot = cartCollection.whereEqualTo("userId", userId).get().await()
            val batch = firestore.batch()
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("CustomerRepo", "Error clearing cart", e)
        }
    }

    override suspend fun placeOrder(order: Order) {
        ordersCollection.document(order.id).set(order).await()
    }
}
