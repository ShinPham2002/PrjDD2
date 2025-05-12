package com.example.bookapp.view

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.CategoryAddLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class CategoryAddActivity : AppCompatActivity() {
    private lateinit var binding: CategoryAddLayoutBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CategoryAddLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //init  firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
        //handle click, begin upload category
        binding.submitBtn.setOnClickListener{
            validateData()
        }
    }

    private  var category = ""
    private fun validateData() {
       //validate data

        //get  data
        category = binding.categoryEt.text.toString().trim()
        //validate data
        if (category.isEmpty()){
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        }
        else{
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        //show progresss
        progressDialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add in firebase db
        val hashMap = HashMap<String,Any>()
        hashMap["id"]= "$timestamp"
        hashMap["category"]= category
        hashMap["timestamp"]= timestamp
        hashMap["uid"]= "${firebaseAuth.uid}"

        //add to firebase db: Database Root > Categories > categoryId > category info
        var ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref .child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Add successfully...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                //failed to add
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to{${e.message}", Toast.LENGTH_SHORT).show()

            }
    }


}