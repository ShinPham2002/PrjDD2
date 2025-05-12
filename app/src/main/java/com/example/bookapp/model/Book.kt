package com.example.bookapp.model

data class Book(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val category: String = "",
    val viewsCount: Long = 0,
    val downloadsCount: Long = 0,
    val url: String = "",
    val timestamp: Long = 0
)
