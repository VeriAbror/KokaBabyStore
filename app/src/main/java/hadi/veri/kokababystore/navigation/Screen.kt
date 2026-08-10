package hadi.veri.kokababystore.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Customer Screens
    object CustomerHome : Screen("customer_home")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Cart : Screen("cart")
    object OrderHistory : Screen("order_history")

    // Admin Screens
    object AdminDashboard : Screen("admin_dashboard")
    object AdminOrders : Screen("admin_orders")
    object ProductList : Screen("product_list")
    object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun createRoute(productId: String?) = "add_edit_product?productId=$productId"
    }
}
