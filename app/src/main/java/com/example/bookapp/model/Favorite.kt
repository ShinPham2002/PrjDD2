package com.example.bookapp.model

data class Favorite(
    var id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val timestamp: Long = 0L,
    val createdBy: String = ""
)
