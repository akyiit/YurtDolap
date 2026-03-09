package com.yurtdolap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val isUserAuthenticatedInFirebase: Boolean
        get() = auth.currentUser != null

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override suspend fun signInAnonymously(): Resource<Unit> {
        return try {
            auth.signInAnonymously().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Giriş yapılamadı. E-posta veya şifre hatalı olabilir.")
        }
    }

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        name: String,
        city: String,
        dormitory: String
    ): Resource<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
            
            if (userId != null) {
                // Kayıt başarılı, kullanıcı bilgilerini Firestore'a ekle
                val userMap = hashMapOf(
                    "id" to userId,
                    "name" to name,
                    "email" to email,
                    "city" to city,
                    "dormitory" to dormitory,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(userId).set(userMap).await()
                Resource.Success(Unit)
            } else {
                Resource.Error("Kullanıcı oluşturuldu ancak ID alınamadı.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Kayıt işlemi başarısız oldu.")
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Çıkış yapılamadı")
        }
    }
}
