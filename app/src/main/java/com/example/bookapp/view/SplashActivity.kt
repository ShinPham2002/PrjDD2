package com.example.bookapp.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.R
import com.example.bookapp.databinding.LoginLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: LoginLayoutBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splash_layout)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            checkUser()
        }, 2000) // delay 2 giây
    }

    private fun checkUser() {
        try {
            //get current user, if logged in or not
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                //user not logged in, goto main screen
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                //user logged in, check user type
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                //get User type e.g. user or admin
                                val userType = snapshot.child("userType").value as? String
                                when (userType) {
                                    "user" -> {
                                        //its simple user, open user dashboard
                                        startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                                        finish()
                                    }
                                    "admin" -> {
                                        startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                                        finish()
                                    }
                                    else -> {
                                        //unknown user type, goto main screen
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                        finish()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("SplashActivity", "Error checking user type", e)
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("SplashActivity", "Database error: ${error.message}")
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                    })
            }
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in checkUser", e)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
/*Keep user logged in
* 1)  Check if user logged in
* 2)  Check type of user*/