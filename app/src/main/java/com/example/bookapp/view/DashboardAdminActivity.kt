package com.example.bookapp.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.DashbroadAdminLayoutBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardAdminActivity: AppCompatActivity() {

    private lateinit var binding: DashbroadAdminLayoutBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DashbroadAdminLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init  firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        // handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
        //handle click, start add category page
         binding.addCategoryBtn.setOnClickListener {
             startActivity(Intent(this, CategoryAddActivity::class.java))
         }

    }
    private fun checkUser() {
        //get current user
        val firebaseUser  = firebaseAuth.currentUser
        if (firebaseUser ==  null){
            //not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{
            //logged in, get and show user info
            val  email = firebaseUser.email
            //set to textView of toolBar
            binding.subTitleTv.text = email
        }
    }
}