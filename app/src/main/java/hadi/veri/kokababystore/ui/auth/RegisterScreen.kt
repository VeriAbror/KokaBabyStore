package hadi.veri.kokababystore.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(navController: NavController, authService: AuthService) {
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(authService)
    )

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val registerState by viewModel.registerState.collectAsState()

    // Show a dialog on success
    if (registerState is RegisterState.Success) {
        AlertDialog(
            onDismissRequest = { /* Do nothing */ },
            title = { Text("Registration Successful") },
            text = { Text((registerState as RegisterState.Success).message) },
            confirmButton = {
                Button(onClick = { navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                } }) {
                    Text("Go to Login")
                }
            }
        )
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
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Create an Account", style = MaterialTheme.typography.headlineMedium)
                
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (registerState is RegisterState.Error) {
                    Text(text = (registerState as RegisterState.Error).message, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = { viewModel.registerUser(fullName, email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = registerState !is RegisterState.Loading
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Register", color = Color.Black)
                    }
                }
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Already have an account? Login", color = PrimaryBlue)
                }
            }
        }
    }
}
