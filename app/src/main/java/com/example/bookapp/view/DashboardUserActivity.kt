package com.example.bookapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.R
import com.example.bookapp.adapter.AdapterBook
import com.example.bookapp.databinding.DashbroadUserLayoutBinding
import com.example.bookapp.model.Book
import com.example.bookapp.model.Category
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: DashbroadUserLayoutBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var bookList: ArrayList<Book>
    private lateinit var categoryList: ArrayList<Category>
    private lateinit var adapterBook: AdapterBook

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DashbroadUserLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //init lists
        bookList = ArrayList()
        categoryList = ArrayList()

        //setup RecyclerView
        binding.booksRv.layoutManager = LinearLayoutManager(this)
        adapterBook = AdapterBook(this, bookList)
        binding.booksRv.adapter = adapterBook

        //load categories
        loadCategories()

        //load books
        loadBooks()

        //handle search
        binding.searchEt.setOnClickListener {
            // TODO: Implement search functionality
            Toast.makeText(this, "Tính năng tìm kiếm đang được phát triển", Toast.LENGTH_SHORT).show()
        }

        //handle logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.child("userType").value
                        if (userType == "admin") {
                            startActivity(Intent(this@DashboardUserActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DashboardUserActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun loadCategories() {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                binding.categoriesChipGroup.removeAllViews()
                
                // Add "Tất cả" chip
                val allChip = Chip(this@DashboardUserActivity)
                allChip.text = "Tất cả"
                allChip.isCheckable = true
                allChip.isChecked = true
                allChip.setOnClickListener {
                    loadBooks()
                }
                binding.categoriesChipGroup.addView(allChip)

                for (ds in snapshot.children) {
                    try {
                        val category = ds.getValue(Category::class.java)
                        if (category != null) {
                            categoryList.add(category)
                            
                            // Add category chip
                            val chip = Chip(this@DashboardUserActivity)
                            chip.text = category.category
                            chip.isCheckable = true
                            chip.setOnClickListener {
                                filterBooksByCategory(category.category)
                            }
                            binding.categoriesChipGroup.addView(chip)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DashboardUserActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardUserActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadBooks() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookList.clear()
                for (ds in snapshot.children) {
                    try {
                        val book = ds.getValue(Book::class.java)
                        if (book != null) {
                            bookList.add(book)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DashboardUserActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                adapterBook = AdapterBook(this@DashboardUserActivity, bookList)
                binding.booksRv.layoutManager = LinearLayoutManager(this@DashboardUserActivity)
                binding.booksRv.adapter = adapterBook
                binding.progressBar.visibility = android.view.View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@DashboardUserActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterBooksByCategory(category: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("category").equalTo(category)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val book = ds.getValue(Book::class.java)
                            if (book != null) {
                                bookList.add(book)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@DashboardUserActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    adapterBook.notifyDataSetChanged()
                    binding.progressBar.visibility = android.view.View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@DashboardUserActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}