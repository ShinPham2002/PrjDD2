package com.example.bookapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowCommentBinding
import com.example.bookapp.model.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdapterComment(
    private val context: Context,
    private var commentList: ArrayList<Comment>
) : RecyclerView.Adapter<AdapterComment.HolderComment>() {

    inner class HolderComment(private val binding: RowCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            try {
                //set comment content
                binding.commentTv.text = comment.content

                //format timestamp
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(comment.timestamp))
                binding.dateTv.text = formattedDate

                //load user info
                loadUserInfo(comment.userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun loadUserInfo(userId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val name = snapshot.child("name").value as? String
                            binding.nameTv.text = name ?: "Người dùng"
                        } catch (e: Exception) {
                            binding.nameTv.text = "Người dùng"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.nameTv.text = "Người dùng"
                    }
                })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        val binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = commentList.size
} 