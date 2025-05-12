package com.example.bookapp.controller

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object DashboardController {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Lấy thống kê tổng số sách và người dùng từ Firestore
     */
    fun getDashboardStats(onResult: (Map<String, Any>?) -> Unit) {
        val stats = mutableMapOf<String, Any>()

        firestore.collection("books").get()
            .addOnSuccessListener { books ->
                stats["totalBooks"] = books.size()

                firestore.collection("users").get()
                    .addOnSuccessListener { users ->
                        stats["totalUsers"] = users.size()
                        onResult(stats)
                    }
                    .addOnFailureListener { e ->
                        Log.e("DashboardController", "Lỗi lấy users: ", e)
                        onResult(null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DashboardController", "Lỗi lấy books: ", e)
                onResult(null)
            }
    }

    /**
     * Kiểm tra người dùng hiện tại có phải là admin không
     */
    fun isAdmin(onResult: (Boolean) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val isAdmin = doc.getBoolean("isAdmin") ?: false
                    onResult(isAdmin)
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardController", "Lỗi kiểm tra admin: ", e)
                    onResult(false)
                }
        } else {
            Log.w("DashboardController", "Người dùng chưa đăng nhập.")
            onResult(false)
        }
    }
}
