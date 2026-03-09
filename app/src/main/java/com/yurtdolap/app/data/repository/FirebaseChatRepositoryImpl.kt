package com.yurtdolap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yurtdolap.app.domain.model.ChatRoom
import com.yurtdolap.app.domain.model.Message
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatRepository {

    private val chatsCollection = firestore.collection("chats")

    override suspend fun createOrGetChatRoom(
        otherUserId: String,
        productId: String,
        productTitle: String,
        productImageUrl: String
    ): Resource<String> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanıcı girişi yapılmadı")
        
        return try {
            // Sort to ensure array equality match
            val sortedParticipants = listOf(currentUserId, otherUserId).sorted()
            
            val existingRooms = chatsCollection
                .whereEqualTo("productId", productId)
                .whereEqualTo("participants", sortedParticipants)
                .get()
                .await()
                
            if (!existingRooms.isEmpty) {
                return Resource.Success(existingRooms.documents.first().id)
            }
            
            // Create new room if none exists
            val newChatId = UUID.randomUUID().toString()
            val newRoom = ChatRoom(
                id = newChatId,
                participants = sortedParticipants,
                productId = productId,
                productTitle = productTitle,
                productImageUrl = productImageUrl,
                lastMessage = "Sohbet başladı",
                lastMessageTimestamp = System.currentTimeMillis()
            )
            
            chatsCollection.document(newChatId).set(newRoom).await()
            Resource.Success(newChatId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Oda oluşturulamadı: Hata oluştu")
        }
    }

    override fun getUserChatRooms(): Flow<Resource<List<ChatRoom>>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(Resource.Error("Kullanıcı girişi yapılmadı"))
            close()
            return@callbackFlow
        }

        val listener = chatsCollection
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Bilinmeyen hata"))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val rooms = snapshot.documents.mapNotNull { it.toObject(ChatRoom::class.java) }
                    trySend(Resource.Success(rooms))
                }
            }
            
        awaitClose { listener.remove() }
    }

    override fun getChatMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        val messagesCollection = chatsCollection.document(chatId).collection("messages")
        
        val listener = messagesCollection
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Bilinmeyen hata"))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    trySend(Resource.Success(msgs))
                }
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(chatId: String, text: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanıcı girişi yapılmadı")
        
        return try {
            val messagesCollection = chatsCollection.document(chatId).collection("messages")
            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            val message = Message(
                id = messageId,
                senderId = currentUserId,
                text = text,
                timestamp = timestamp
            )
            
            messagesCollection.document(messageId).set(message).await()
            
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to timestamp
                )
            ).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Mesaj gönderilemedi")
        }
    }
}
