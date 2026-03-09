package com.yurtdolap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override fun getFavoriteProductIds(): Flow<Resource<List<String>>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(Resource.Error("Kullanıcı girişi yapılmadı"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading())

        val listener = usersCollection.document(currentUserId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Favoriler yüklenemedi"))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val favorites = snapshot.get("favoriteProductIds") as? List<String> ?: emptyList()
                trySend(Resource.Success(favorites))
            } else {
                trySend(Resource.Success(emptyList()))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun toggleFavorite(productId: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanıcı girişi yapılmadı")

        return try {
            val userDocRef = usersCollection.document(currentUserId)
            val snapshot = userDocRef.get().await()

            if (!snapshot.exists()) {
                // Initialize user doc if it doesn't exist
                userDocRef.set(mapOf("favoriteProductIds" to listOf(productId))).await()
            } else {
                val favorites = snapshot.get("favoriteProductIds") as? List<String> ?: emptyList()
                if (favorites.contains(productId)) {
                    // Remove
                    userDocRef.update("favoriteProductIds", FieldValue.arrayRemove(productId)).await()
                } else {
                    // Add
                    userDocRef.update("favoriteProductIds", FieldValue.arrayUnion(productId)).await()
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Favori işlemi başarısız oldu")
        }
    }

    override suspend fun getUserProfile(): Resource<com.yurtdolap.app.domain.model.UserProfileData> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanıcı girişi yapılmadı")

        return try {
            val snapshot = usersCollection.document(currentUserId).get().await()
            if (snapshot.exists()) {
                val userProfile = snapshot.toObject(com.yurtdolap.app.domain.model.UserProfileData::class.java)
                if (userProfile != null) {
                    Resource.Success(userProfile)
                } else {
                    Resource.Error("Kullanıcı verisi ayrıştırılamadı")
                }
            } else {
                Resource.Error("Kullanıcı profili bulunamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Profil bilgileri alınamadı")
        }
    }
}
