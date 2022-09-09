package com.example.appbookremakekotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appbookremakekotlin.adapters.AdapterPdfUserHome
import com.example.appbookremakekotlin.adapters.PdfAdminAdapter
import com.example.appbookremakekotlin.adapters.PdfUserAdapter
import com.example.appbookremakekotlin.databinding.ActivityDashboardUserHomeBinding
import com.example.appbookremakekotlin.model.PDFModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.reflect.Array

class DashboardUserHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserHomeBinding
    private lateinit var adapterPdfUserHome: AdapterPdfUserHome

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var pdfArrayList: ArrayList<PDFModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadPdfAllHome()

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this@DashboardUserHomeActivity, ProfileActivity::class.java))
        }

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this@DashboardUserHomeActivity, MainActivity::class.java))
            finish()
        }

        binding.card1.setOnClickListener {
            startActivity(Intent(this@DashboardUserHomeActivity, DashboardUserActivity::class.java))
        }

        binding.card2.setOnClickListener {
            startActivity(Intent(this@DashboardUserHomeActivity, DashboardListBookActivity::class.java))
        }

    }

    private fun loadPdfAllHome() {
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(PDFModel::class.java)

                    if(model != null) {
                        pdfArrayList.add(model!!)
                    }
                }
                adapterPdfUserHome = AdapterPdfUserHome(this@DashboardUserHomeActivity, pdfArrayList)
                binding.allRv.apply {
                    layoutManager = LinearLayoutManager(this@DashboardUserHomeActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = adapterPdfUserHome
                }
                binding.mostViewedRv.apply {
                    layoutManager = LinearLayoutManager(this@DashboardUserHomeActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = adapterPdfUserHome
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardUserHomeActivity, "Failed Book", Toast.LENGTH_LONG).show()
            }

        })
    }


    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null) {
            binding.subTitleTv.text = "Not Logged In"

            binding.btnProfile.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        } else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email

            binding.btnProfile.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }
}