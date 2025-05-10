package com.example.bookapp.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.RegisterLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import java.util.regex.Pattern
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterLayoutBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    companion object {
        private const val TIMEOUT_DURATION = 15L // 15 giây timeout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("Users")

        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click,begin register
        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //1) Input Data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //2) Validate Data
        if (binding.nameEt.text.toString().isEmpty()) {
            Toast.makeText(this, "Enter name...", Toast.LENGTH_SHORT).show()
        } else if (binding.emailEt.text.toString().isEmpty()) {
            Toast.makeText(this, "Enter email...", Toast.LENGTH_SHORT).show()
        } else if (binding.passwordEt.text.toString().isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else if (binding.cPasswordEt.text.toString().isEmpty()) {
            Toast.makeText(this, "Confirm password...", Toast.LENGTH_SHORT).show()
        } else if (binding.passwordEt.text.toString() != binding.cPasswordEt.text.toString()) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$")
        return pattern.matcher(password).matches()
    }

    private fun createUserAccount() {
        progressDialog.setMessage("Đang tạo tài khoản...")
        progressDialog.show()

        val timeoutTask = Runnable {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
                Toast.makeText(this, "Kết nối quá thời gian. Vui lòng thử lại", Toast.LENGTH_SHORT).show()
            }
        }

        android.os.Handler().postDelayed(timeoutTask, TimeUnit.SECONDS.toMillis(TIMEOUT_DURATION))

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                android.os.Handler().removeCallbacks(timeoutTask)
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                android.os.Handler().removeCallbacks(timeoutTask)
                progressDialog.dismiss()
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Đang lưu thông tin người dùng...")

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid ?: return

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp
        hashMap["createdBy"] = "LuongMarus $timestamp"
        hashMap["isBlocked"] = false
        hashMap["isOnline"] = true
        hashMap["lastSeen"] = timestamp

        val timeoutTask = Runnable {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
                Toast.makeText(this, "Kết nối quá thời gian. Vui lòng thử lại", Toast.LENGTH_SHORT).show()
            }
        }

        android.os.Handler().postDelayed(timeoutTask, TimeUnit.SECONDS.toMillis(TIMEOUT_DURATION))

        usersRef.child(uid)
            .setValue(hashMap)
            .addOnSuccessListener {
                android.os.Handler().removeCallbacks(timeoutTask)
                progressDialog.dismiss()
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                android.os.Handler().removeCallbacks(timeoutTask)
                progressDialog.dismiss()
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
