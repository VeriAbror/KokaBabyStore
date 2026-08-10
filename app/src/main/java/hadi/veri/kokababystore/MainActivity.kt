package hadi.veri.kokababystore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import hadi.veri.kokababystore.data.repository.AdminRepository
import hadi.veri.kokababystore.data.repository.AdminRepositoryImpl
import hadi.veri.kokababystore.data.repository.CustomerRepository
import hadi.veri.kokababystore.data.repository.CustomerRepositoryImpl
import hadi.veri.kokababystore.data.service.AuthService
import hadi.veri.kokababystore.data.service.OrderService
import hadi.veri.kokababystore.data.service.impl.AuthServiceImpl
import hadi.veri.kokababystore.data.service.impl.OrderServiceImpl
import hadi.veri.kokababystore.navigation.Screen
import hadi.veri.kokababystore.ui.admin.*
import hadi.veri.kokababystore.ui.auth.LoginScreen
import hadi.veri.kokababystore.ui.auth.RegisterScreen
import hadi.veri.kokababystore.ui.customer.*
import hadi.veri.kokababystore.ui.theme.KokaBabyStoreTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.plugins.auth.Auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://ybayyfesqarorpqprzmm.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InliYXl5ZmVzcWFyb3JwcXByem1tIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjExMDA5MDgsImV4cCI6MjA3NjY3NjkwOH0.H_Lk-bQqrb-OAUYhMoLlcvsWqS5_NB07NNsAOXdyIWg"
        ) {
            install(Storage)
        }
    }

    private val authService: AuthService by lazy {
        AuthServiceImpl(FirebaseAuth.getInstance(), firestore)
    }
    private val orderService: OrderService by lazy {
        OrderServiceImpl(applicationContext, firestore, FirebaseDatabase.getInstance())
    }
    private val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(firestore, supabaseClient.storage)
    }
    private val customerRepository: CustomerRepository by lazy {
        CustomerRepositoryImpl(firestore)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Handle permission grant or denial */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()

        // Subscribe admins to the "new_orders" topic
        lifecycleScope.launch {
            authService.currentUser?.uid?.let { uid ->
                val user = authService.getUser(uid)
                if (user?.role == "admin") {
                    Log.d("MainActivity", "User is admin, subscribing to 'new_orders' topic.")
                    FirebaseMessaging.getInstance().subscribeToTopic("new_orders")
                }
            }
        }

        setContent {
            KokaBabyStoreTheme {
                AppNavHost(
                    authService = authService,
                    orderService = orderService,
                    adminRepository = adminRepository,
                    customerRepository = customerRepository,
                    firestore = firestore
                )
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authService: AuthService,
    orderService: OrderService,
    adminRepository: AdminRepository,
    customerRepository: CustomerRepository,
    firestore: FirebaseFirestore
) {
    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(adminRepository, orderService, authService, firestore)
    )
    val customerViewModel: CustomerViewModel = viewModel(
        factory = CustomerViewModelFactory(adminRepository, customerRepository, authService, orderService)
    )

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, authService = authService)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authService = authService)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, authService = authService)
        }
        composable(Screen.CustomerHome.route) {
            CustomerHomeScreen(
                navController = navController,
                viewModel = customerViewModel
            )
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            productId?.let {
                ProductDetailScreen(
                    navController = navController,
                    productId = it,
                    viewModel = customerViewModel
                )
            }
        }
        composable(Screen.Cart.route) {
            CartScreen(
                navController = navController,
                viewModel = customerViewModel
            )
        }
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                navController = navController,
                viewModel = customerViewModel
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                viewModel = adminViewModel
            )
        }
        composable(Screen.AdminOrders.route) {
            AdminOrderScreen(
                navController = navController,
                viewModel = adminViewModel
            )
        }
        composable(Screen.ProductList.route) {
            ProductListScreen(
                navController = navController,
                viewModel = adminViewModel
            )
        }
        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(navArgument("productId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AddEditProductScreen(
                navController = navController,
                productId = productId,
                viewModel = adminViewModel
            )
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController, authService: AuthService) {
    val isInPreview = LocalInspectionMode.current

    LaunchedEffect(key1 = true) {
        if (isInPreview) return@LaunchedEffect // Correctly use the value here

        val firebaseUser = authService.currentUser
        if (firebaseUser != null) {
            try {
                val user = authService.getUser(firebaseUser.uid)
                val route = if (user?.role == "admin") Screen.AdminDashboard.route else Screen.CustomerHome.route
                navController.navigate(route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "Failed to get user data.", e)
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = "https://ybayyfesqarorpqprzmm.supabase.co/storage/v1/object/public/promo/banner%201.png",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KokaBabyStoreTheme {
        Scaffold {
            Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
                Text(text = "Koka Baby Store")
            }
        }
    }
}
