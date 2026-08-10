const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Cloud Function to send a notification to all admins when a new order is placed.
 * Triggered when a new document is created in the 'orders' collection.
 */
exports.sendNewOrderNotification = functions.firestore
  .document("orders/{orderId}")
  .onCreate(async (snap, context) => {
    const newOrder = snap.data();

    // 1. Get all admin user tokens
    const adminsSnapshot = await db.collection("users").where("role", "==", "admin").get();
    if (adminsSnapshot.empty) {
      console.log("No admin users found.");
      return;
    }

    const tokens = [];
    adminsSnapshot.forEach(doc => {
      const token = doc.data().fcmToken;
      if (token) {
        tokens.push(token);
      }
    });

    if (tokens.length === 0) {
      console.log("No admin tokens found for sending notification.");
      return;
    }

    // 2. Construct the notification payload
    const payload = {
      notification: {
        title: "New Order Received!",
        body: `A new order #${context.params.orderId.substring(0, 6)} has been placed for Rp ${newOrder.totalAmount}.`,
        sound: "default",
      },
    };

    // 3. Send the notification to all admin tokens
    try {
      const response = await messaging.sendToDevice(tokens, payload);
      console.log("Successfully sent new order notification to admins:", response);
    } catch (error) {
      console.error("Error sending new order notification:", error);
    }

    // 4. Update the real-time database counter for pending orders
    const counterRef = admin.database().ref("order_alerts/summary/totalPending");
    return counterRef.transaction((current) => {
        return (current || 0) + 1;
    });
  });

/**
 * Cloud Function to send a notification to a customer when their order status is updated.
 * Triggered when a document in the 'orders' collection is updated.
 */
exports.sendOrderStatusUpdate = functions.firestore
  .document("orders/{orderId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    // Check if the 'status' field has actually changed
    if (before.status === after.status) {
      console.log("Status has not changed. No notification sent.");
      return null;
    }

    // 1. Get the customer's FCM token
    const customerId = after.customerId;
    const userSnapshot = await db.collection("users").doc(customerId).get();
    if (!userSnapshot.exists) {
      console.error(`User with ID ${customerId} not found.`);
      return null;
    }

    const fcmToken = userSnapshot.data().fcmToken;
    if (!fcmToken) {
      console.log(`FCM token not found for user ${customerId}.`);
      return null;
    }

    // 2. Construct the notification payload
    const payload = {
      notification: {
        title: "Order Status Update",
        body: `Hi! Your order #${context.params.orderId.substring(0, 6)} is now '${after.status}'.`,
        sound: "default",
      },
    };

    // 3. Send the notification
    try {
      const response = await messaging.sendToDevice([fcmToken], payload);
      console.log("Successfully sent status update notification:", response);
    } catch (error) {
      console.error("Error sending status update notification:", error);
    }

    // 4. Update pending order counter if status has changed to or from 'pending'
    const counterRef = admin.database().ref("order_alerts/summary/totalPending");
    if (before.status === 'pending' && after.status !== 'pending') {
        return counterRef.transaction((current) => {
            return (current || 1) - 1;
        });
    } else if (before.status !== 'pending' && after.status === 'pending') {
        return counterRef.transaction((current) => {
            return (current || 0) + 1;
        });
    }

    return null;
  });
