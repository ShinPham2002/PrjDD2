package com.example.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowBookBinding
import com.example.bookapp.model.Book
import com.example.bookapp.model.Favorite
import com.example.bookapp.view.BookDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class AdapterBook(
    private val context: Context,
    private val bookList: ArrayList<Book>
) : RecyclerView.Adapter<AdapterBook.HolderBook>(), Filterable {

    private val filterList: ArrayList<Book> = ArrayList(bookList)
    private lateinit var firebaseAuth: FirebaseAuth

    init {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): HolderBook {
        val binding = RowBookBinding.inflate(android.view.LayoutInflater.from(context), parent, false)
        return HolderBook(binding)
    }

    override fun onBindViewHolder(holder: HolderBook, position: Int) {
        val model = bookList[position]
        val bookId = model.id
        val title = model.title
        val description = model.description
        val categoryId = model.categoryId
        val category = model.category
        val viewsCount = model.viewsCount
        val downloadsCount = model.downloadsCount
        val url = model.url
        val timestamp = model.timestamp

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.categoryTv.text = category
        holder.viewsTv.text = viewsCount.toString()
        holder.downloadsTv.text = downloadsCount.toString()

        try {
            //set image
            try {
                Picasso.get()
                    .load(url)
                    .into(holder.bookIv)
            } catch (e: Exception) {
                holder.bookIv.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            //check if book is favorite
            holder.checkFavoriteStatus(bookId)

            //handle click
            holder.itemView.setOnClickListener {
                //increment views count
                holder.incrementViewsCount(bookId)
                
                //open book detail activity
                val intent = Intent(context, BookDetailActivity::class.java)
                intent.putExtra("bookId", bookId)
                context.startActivity(intent)
            }

            //handle favorite click
            holder.favoriteBtn.setOnClickListener {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    holder.toggleFavorite(bookId, currentUser.uid)
                } else {
                    Toast.makeText(context, "Vui lòng đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = bookList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null && constraint.isNotEmpty()) {
                    val filteredList = ArrayList<Book>()
                    val searchPattern = constraint.toString().lowercase()
                    for (book in filterList) {
                        if (book.title.lowercase().contains(searchPattern) ||
                            book.description.lowercase().contains(searchPattern) ||
                            book.category.lowercase().contains(searchPattern)
                        ) {
                            filteredList.add(book)
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                } else {
                    results.count = filterList.size
                    results.values = filterList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    val filteredList = results.values as ArrayList<Book>
                    bookList.clear()
                    for (book in filteredList) {
                        bookList.add(book)
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class HolderBook(private val binding: RowBookBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val viewsTv = binding.viewsTv
        val downloadsTv = binding.downloadsTv
        val bookIv = binding.bookIv
        val favoriteBtn = binding.favoriteBtn

        fun checkFavoriteStatus(bookId: String) {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val ref = FirebaseDatabase.getInstance().getReference("Favorites")
                ref.orderByChild("bookId").equalTo(bookId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var isFavorite = false
                            for (ds in snapshot.children) {
                                val favorite = ds.getValue(Favorite::class.java)
                                if (favorite?.userId == currentUser.uid) {
                                    isFavorite = true
                                    break
                                }
                            }
                            favoriteBtn.setImageResource(
                                if (isFavorite) android.R.drawable.btn_star_big_on
                                else android.R.drawable.btn_star_big_off
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        fun toggleFavorite(bookId: String, userId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Favorites")
            ref.orderByChild("bookId").equalTo(bookId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var isFavorite = false
                        var favoriteId = ""
                        
                        for (ds in snapshot.children) {
                            val favorite = ds.getValue(Favorite::class.java)
                            if (favorite?.userId == userId) {
                                isFavorite = true
                                favoriteId = ds.key ?: ""
                                break
                            }
                        }

                        if (isFavorite) {
                            //remove from favorites
                            ref.child(favoriteId).removeValue()
                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                        } else {
                            //add to favorites
                            val favorite = Favorite(
                                id = "",
                                bookId = bookId,
                                userId = userId,
                                timestamp = System.currentTimeMillis(),
                                createdBy = "LuongMarus"
                            )
                            val newFavoriteId = ref.push().key ?: ""
                            favorite.id = newFavoriteId
                            ref.child(newFavoriteId).setValue(favorite)
                            Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        fun incrementViewsCount(bookId: String) {
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
    }
} 