package com.example.appbookremakekotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.appbookremakekotlin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {

        }

        binding.registerBtn.setOnClickListener {

            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        name = binding.nameEt.text.toString()
        email = binding.emailEt.text.toString()
        password = binding.passwordEt.text.toString()
        var confirmPassword = binding.confirmPasswordEt.text.toString()

        if(name.isEmpty()) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_LONG).show()
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this," Invalid Email Pattern", Toast.LENGTH_LONG).show()
        } else if(password.isEmpty()) {
            Toast.makeText(this,"Enter Password", Toast.LENGTH_LONG).show()
        } else if(confirmPassword.isEmpty()) {
            Toast.makeText(this,"Enter Confirm Password", Toast.LENGTH_LONG).show()
        } else if(password != confirmPassword) {
            Toast.makeText(this,"Password doesn't match", Toast.LENGTH_LONG).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed create account: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun updateUserInfo() {

        progressDialog.setMessage("Saving User Info...")

        val timestamp = System.currentTimeMillis()
        //val uid = firebaseAuth.uid
        //..
        val user: FirebaseUser? = firebaseAuth.currentUser
        val uid: String = user!!.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        /*hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp*/
        hashMap.put("uid",uid)
        hashMap.put("email",email)
        hashMap.put("name",name)
        hashMap.put("profileImage", "")
        hashMap.put("userType","user")
        hashMap.put("timestamp", timestamp)

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Account Created...", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserHomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed create account: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}