package com.example.bookapp.controller

import com.example.bookapp.model.Book
import com.google.firebase.firestore.FirebaseFirestore

object BookController {
    private val firestore = FirebaseFirestore.getInstance()

    fun addBook(book: Book, onComplete: (Boolean, String?) -> Unit) {
        val newBook = book.copy(timestamp = System.currentTimeMillis())
        firestore.collection("books")
            .add(newBook)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun updateBook(book: Book, onComplete: (Boolean, String?) -> Unit) {
        if (book.id.isEmpty()) return onComplete(false, "Book ID is missing")
        firestore.collection("books")
            .document(book.id)
            .set(book)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun deleteBook(bookId: String, onComplete: (Boolean, String?) -> Unit) {
        firestore.collection("books")
            .document(bookId)
            .delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getBookById(bookId: String, onResult: (Book?) -> Unit) {
        firestore.collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                val book = document.toObject(Book::class.java)
                onResult(book)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getAllBooks(onResult: (List<Book>) -> Unit) {
        firestore.collection("books")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val books = documents.toObjects(Book::class.java)
                onResult(books)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getBooksByCategory(categoryId: String, onResult: (List<Book>) -> Unit) {
        firestore.collection("books")
            .whereEqualTo("categoryId", categoryId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val books = documents.toObjects(Book::class.java)
                onResult(books)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun searchBooks(query: String, onResult: (List<Book>) -> Unit) {
        firestore.collection("books")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val books = documents.toObjects(Book::class.java)
                onResult(books)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}