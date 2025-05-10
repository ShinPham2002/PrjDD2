package com.example.bookapp.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.adapter.AdapterComment
import com.example.bookapp.databinding.ActivityBookDetailBinding
import com.example.bookapp.model.Book
import com.example.bookapp.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class BookDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var commentList: ArrayList<Comment>
    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //get book id from intent
        val bookId = intent.getStringExtra("bookId")
        if (bookId != null) {
            loadBookDetails(bookId)
            loadComments(bookId)
        }

        //handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle download button click
        binding.downloadBtn.setOnClickListener {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                incrementDownloadsCount(bookId ?: "")
                Toast.makeText(this, "Đang tải xuống...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để tải sách", Toast.LENGTH_SHORT).show()
            }
        }

        //handle add comment button click
        binding.addCommentBtn.setOnClickListener {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val comment = binding.commentEt.text.toString().trim()
                if (comment.isNotEmpty()) {
                    addComment(bookId ?: "", comment, currentUser.uid)
                    binding.commentEt.text?.clear()
                } else {
                    Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBookDetails(bookId: String) {
        binding.progressBar.visibility = View.VISIBLE
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val book = snapshot.getValue(Book::class.java)
                        if (book != null) {
                            //set data
                            binding.titleTv.text = book.title
                            binding.descriptionTv.text = book.description
                            binding.categoryTv.text = book.category
                            binding.viewsTv.text = "${book.viewsCount} lượt xem"
                            binding.downloadsTv.text = "${book.downloadsCount} lượt tải"

                            //set image
                            try {
                                Picasso.get()
                                    .load(book.url)
                                    .into(binding.bookIv)
                            } catch (e: Exception) {
                                binding.bookIv.setImageResource(android.R.drawable.ic_menu_gallery)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@BookDetailActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@BookDetailActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadComments(bookId: String) {
        commentList = ArrayList()
        binding.commentsRv.layoutManager = LinearLayoutManager(this)
        adapterComment = AdapterComment(this, commentList)
        binding.commentsRv.adapter = adapterComment

        val ref = FirebaseDatabase.getInstance().getReference("Comments")
        ref.orderByChild("bookId").equalTo(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val comment = ds.getValue(Comment::class.java)
                            if (comment != null) {
                                commentList.add(comment)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@BookDetailActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    adapterComment.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@BookDetailActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addComment(bookId: String, content: String, userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Comments")
        val comment = Comment(
            id = "",
            bookId = bookId,
            userId = userId,
            content = content,
            timestamp = System.currentTimeMillis(),
            createdBy = "LuongMarus"
        )
        val commentId = ref.push().key ?: ""
        comment.id = commentId
        ref.child(commentId).setValue(comment)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun incrementViewsCount(bookId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentViews = snapshot.child("viewsCount").getValue(Long::class.java) ?: 0
                    val hashMap = HashMap<String, Any>()
                    hashMap["viewsCount"] = currentViews + 1
                    ref.child(bookId)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun incrementDownloadsCount(bookId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentDownloads = snapshot.child("downloadsCount").getValue(Long::class.java) ?: 0
                    val hashMap = HashMap<String, Any>()
                    hashMap["downloadsCount"] = currentDownloads + 1
                    ref.child(bookId)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
} 