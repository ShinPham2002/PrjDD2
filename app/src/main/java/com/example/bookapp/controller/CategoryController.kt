package com.example.bookapp.controller

import com.google.firebase.database.FirebaseDatabase
import com.example.bookapp.model.Category

object CategoryController {
    private val dbRef = FirebaseDatabase.getInstance().getReference("Categories")

    fun addCategory(category: Category, onComplete: (Boolean, String?) -> Unit) {
        val id = dbRef.push().key ?: return onComplete(false, "Cannot generate ID")
        dbRef.child(id).setValue(category.copy(id = id))
            .addOnCompleteListener {
                onComplete(it.isSuccessful, it.exception?.message)
            }
    }
}
