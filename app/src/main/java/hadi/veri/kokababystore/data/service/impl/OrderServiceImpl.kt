package hadi.veri.kokababystore.data.service.impl

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import hadi.veri.kokababystore.R
import hadi.veri.kokababystore.data.OrderAlert
import hadi.veri.kokababystore.data.service.OrderService
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Using the specific database URL for the asia-southeast1 region
private const val DATABASE_URL = "https://koka-baby-store-default-rtdb.asia-southeast1.firebasedatabase.app/"

class OrderServiceImpl(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    // The original database instance is no longer used directly
    private val database: FirebaseDatabase 
) : OrderService {

    // Use a database instance that points to a specific URL
    private val db = FirebaseDatabase.getInstance(DATABASE_URL)

    private val fcmScope = "https://www.googleapis.com/auth/cloud-platform"

    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account)
            val credentials = GoogleCredentials.fromStream(inputStream).createScoped(fcmScope)
            credentials.refresh()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("OrderService", "Error getting access token", e)
            ""
        }
    }

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { 
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true 
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val token = getAccessToken()
                    if (token.isNotEmpty()) {
                        BearerTokens(token, "")
                    } else {
                        null
                    }
                }
                refreshTokens {
                     val token = getAccessToken()
                    if (token.isNotEmpty()) {
                        BearerTokens(token, "")
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun listenToOrderAlerts(): Flow<OrderAlert> = callbackFlow {
        val ref = db.getReference("order_alerts/summary") // Use specific db instance
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.getValue(OrderAlert::class.java) ?: OrderAlert()
                trySend(alert).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun sendFcmMessage(message: JsonObject) {
        // projectId is fetched from strings.xml to avoid hardcoding
        val projectId = context.getString(R.string.project_id)
        val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
        try {
            val response: HttpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(message)
            }
            Log.d("OrderService", "FCM Response: ${response.status} - ${response.bodyAsText()}")
        } catch (e: Exception) {
            Log.e("OrderService", "Error sending FCM message", e)
        }
    }

    override suspend fun sendNewOrderNotification(orderId: String, totalAmount: Double) {
        val message = buildJsonObject { 
            put("message", buildJsonObject {
                put("topic", "new_orders")
                put("notification", buildJsonObject {
                    put("title", "New Order Received!")
                    put("body", "Order #${orderId.take(6)} for Rp $totalAmount has been placed.")
                })
            })
        }
        sendFcmMessage(message)
    }

    override suspend fun sendOrderStatusUpdateNotification(userToken: String, orderId: String, newStatus: String) {
        if (userToken.isBlank()) return
        val message = buildJsonObject { 
            put("message", buildJsonObject {
                put("token", userToken)
                put("notification", buildJsonObject {
                    put("title", "Order Status Updated")
                    put("body", "Your order #${orderId.take(6)} is now $newStatus.")
                })
            })
        }
        sendFcmMessage(message)
    }

    override suspend fun incrementPendingOrderCount() = suspendCancellableCoroutine<Unit> { continuation ->
        val counterRef = db.getReference("order_alerts/summary/totalPending") // Use specific db instance
        counterRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val count = currentData.getValue(Long::class.java) ?: 0L
                currentData.value = count + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    continuation.resumeWithException(error.toException())
                } else if (committed) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("RTDB transaction not committed."))
                }
            }
        })
    }

    override suspend fun decrementPendingOrderCount() = suspendCancellableCoroutine<Unit> { continuation ->
        val counterRef = db.getReference("order_alerts/summary/totalPending") // Use specific db instance
        counterRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val count = currentData.getValue(Long::class.java) ?: 0L
                if (count > 0) {
                    currentData.value = count - 1
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    continuation.resumeWithException(error.toException())
                } else if (committed) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("RTDB transaction not committed."))
                }
            }
        })
    }
}
