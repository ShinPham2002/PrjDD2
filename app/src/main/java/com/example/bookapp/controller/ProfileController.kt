package com.example.bookapp.controller

import com.example.bookapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ProfileController {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getUserProfile(onResult: (User?) -> Unit) {
        val uid = getCurrentUserId()
        if (uid != null) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    onResult(user)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } else {
            onResult(null)
        }
    }

    fun updateUserProfile(updatedUser: User, onResult: (Boolean, String?) -> Unit) {
        val uid = getCurrentUserId()
        if (uid != null) {
            firestore.collection("users").document(uid)
                .set(updatedUser)
                .addOnSuccessListener {
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    onResult(false, e.message)
                }
        } else {
            onResult(false, "Chưa đăng nhập")
        }
    }
}
