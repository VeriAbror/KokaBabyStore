package hadi.veri.kokababystore.data.service

import hadi.veri.kokababystore.data.OrderAlert
import kotlinx.coroutines.flow.Flow

/**
 * Service to interact with real-time features and send notifications related to orders.
 * WARNING: Sending notifications from the client-side is insecure and should not be done in production.
 * This logic should be migrated to a secure backend (e.g., Cloud Functions).
 */
interface OrderService {
    /**
     * Listens to live updates for order alerts, such as the count of pending orders.
     */
    fun listenToOrderAlerts(): Flow<OrderAlert>

    /**
     * Sends a notification to all admin users about a new order.
     * @param orderId The ID of the new order.
     * @param totalAmount The total amount of the new order.
     */
    suspend fun sendNewOrderNotification(orderId: String, totalAmount: Double)

    /**
     * Sends a notification to a specific customer about their order status update.
     * @param userToken The FCM token of the customer.
     * @param orderId The ID of the order that was updated.
     * @param newStatus The new status of the order.
     */
    suspend fun sendOrderStatusUpdateNotification(userToken: String, orderId: String, newStatus: String)

    /**
     * Increments the pending order count in the Realtime Database.
     */
    suspend fun incrementPendingOrderCount()

    /**
     * Decrements the pending order count in the Realtime Database.
     */
    suspend fun decrementPendingOrderCount()
}
