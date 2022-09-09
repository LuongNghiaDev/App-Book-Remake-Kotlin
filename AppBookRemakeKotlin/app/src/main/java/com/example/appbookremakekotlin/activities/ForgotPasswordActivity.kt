package com.example.appbookremakekotlin.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            validateData()
        }
    }

    private var email = ""

    private fun validateData() {
        email = binding.EmailEt.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this," Invalid Email Pattern", Toast.LENGTH_LONG).show()
        } else if(email.isEmpty()) {
            Toast.makeText(this,"Enter Email", Toast.LENGTH_LONG).show()
        } else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        progressDialog.setMessage("Sending password reset instructions to $email...")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Instructions sent to $email", Toast.LENGTH_LONG).show()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Faild to sent $email", Toast.LENGTH_LONG).show()
            }
    }
}