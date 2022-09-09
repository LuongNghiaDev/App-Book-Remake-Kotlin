package com.example.appbookremakekotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.adapters.AdapterCategoryHome
import com.example.appbookremakekotlin.adapters.CategoryAdapter
import com.example.appbookremakekotlin.databinding.ActivityDashboardListBookBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardListBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardListBookBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryList: ArrayList<CategoryModel>
    private lateinit var adapterCategoryHome: AdapterCategoryHome

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardListBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategoriesHome()

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this@DashboardListBookActivity, ProfileActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategoryHome.filter.filter(p0)
                } catch (e:Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }

    private fun loadCategoriesHome() {
        categoryList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()

                for(ds in snapshot.children) {
                    val model = ds.getValue(CategoryModel::class.java)

                    categoryList.add(model!!)
                }

                adapterCategoryHome = AdapterCategoryHome(this@DashboardListBookActivity, categoryList)
                binding.categoriesRv.apply {
                    layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                    adapter = adapterCategoryHome
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null) {
            binding.subTitleTv.text = "Not Logged In"

            binding.btnProfile.visibility = View.GONE
        } else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email

            binding.btnProfile.visibility = View.VISIBLE
        }
    }

}