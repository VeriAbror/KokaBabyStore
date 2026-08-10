package hadi.veri.kokababystore.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import hadi.veri.kokababystore.data.service.AuthService
import hadi.veri.kokababystore.navigation.Screen
import hadi.veri.kokababystore.ui.theme.PrimaryBlue

@Composable
fun LoginScreen(navController: NavController, authService: AuthService) {
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authService)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val user = (loginState as LoginState.Success).user
            val route = if (user.role == "admin") Screen.AdminDashboard.route else Screen.CustomerHome.route
            navController.navigate(route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "https://ybayyfesqarorpqprzmm.supabase.co/storage/v1/object/public/promo/banner%201.png",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium)
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginState is LoginState.Error) {
                    Text(text = (loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = { viewModel.loginUser(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Login", color = Color.Black)
                    }
                }
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text("Don't have an account? Register", color = PrimaryBlue)
                }
            }
        }
    }
}

// A simple factory to provide dependencies to the ViewModel
class LoginViewModelFactory(private val authService: AuthService) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
