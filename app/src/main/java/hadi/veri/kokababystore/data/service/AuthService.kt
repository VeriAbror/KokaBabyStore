package hadi.veri.kokababystore.data.service

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import hadi.veri.kokababystore.data.User
import kotlinx.coroutines.flow.Flow

interface AuthService {
    val currentUser: FirebaseUser?
    val authState: Flow<FirebaseUser?>

    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(user: User, password: String)
    suspend fun logout()
    suspend fun getUser(uid: String): User?
}
