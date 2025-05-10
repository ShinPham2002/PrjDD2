package com.example.bookapp.controller

import com.example.bookapp.model.Favorite
import com.google.firebase.firestore.FirebaseFirestore

object FavoriteController {
    private val firestore = FirebaseFirestore.getInstance()

    fun addToFavorite(bookId: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = ProfileController.getCurrentUserId() ?: return onComplete(false, "Chưa đăng nhập")
        
        val favorite = Favorite(
            bookId = bookId,
            userId = userId,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("favorites")
            .add(favorite)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun removeFromFavorite(bookId: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = ProfileController.getCurrentUserId() ?: return onComplete(false, "Chưa đăng nhập")

        firestore.collection("favorites")
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(false, "Không tìm thấy sách yêu thích")
                    return@addOnSuccessListener
                }

                documents.documents[0].reference.delete()
                    .addOnSuccessListener {
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getFavoriteBooks(onResult: (List<String>) -> Unit) {
        val userId = ProfileController.getCurrentUserId() ?: return onResult(emptyList())

        firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val bookIds = documents.mapNotNull { it.getString("bookId") }
                onResult(bookIds)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
} 