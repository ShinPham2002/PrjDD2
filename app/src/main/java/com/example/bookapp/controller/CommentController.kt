package com.example.bookapp.controller

import com.example.bookapp.model.Comment
import com.google.firebase.firestore.FirebaseFirestore

object CommentController {
    private val firestore = FirebaseFirestore.getInstance()

    fun addComment(bookId: String, content: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = ProfileController.getCurrentUserId() ?: return onComplete(false, "Chưa đăng nhập")
        
        val comment = Comment(
            bookId = bookId,
            userId = userId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("comments")
            .add(comment)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getBookComments(bookId: String, onResult: (List<Comment>) -> Unit) {
        firestore.collection("comments")
            .whereEqualTo("bookId", bookId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val comments = documents.toObjects(Comment::class.java)
                onResult(comments)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun deleteComment(commentId: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = ProfileController.getCurrentUserId() ?: return onComplete(false, "Chưa đăng nhập")

        firestore.collection("comments")
            .document(commentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.getString("userId") != userId) {
                    onComplete(false, "Không có quyền xóa bình luận này")
                    return@addOnSuccessListener
                }

                document.reference.delete()
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
} 