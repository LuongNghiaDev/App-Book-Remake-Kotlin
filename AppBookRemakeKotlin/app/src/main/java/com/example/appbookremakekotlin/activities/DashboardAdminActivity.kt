package com.example.appbookremakekotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.appbookremakekotlin.adapters.CategoryAdapter
import com.example.appbookremakekotlin.databinding.ActivityDashboardAdminBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryList: ArrayList<CategoryModel>
    private lateinit var adapterCategory: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(p0)
                }catch (e:Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this@DashboardAdminActivity, CategoryAddActivity::class.java))
        }

        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this@DashboardAdminActivity, PdfAddActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this@DashboardAdminActivity, ProfileActivity::class.java))
        }
    }

    private fun loadCategories() {
        categoryList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()

                for(ds in snapshot.children) {
                    val model = ds.getValue(CategoryModel::class.java)

                    categoryList.add(model!!)
                }
                adapterCategory = CategoryAdapter(this@DashboardAdminActivity, categoryList)
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null) {
            startActivity(Intent(this@DashboardAdminActivity, MainActivity::class.java))
            finish()
        } else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}