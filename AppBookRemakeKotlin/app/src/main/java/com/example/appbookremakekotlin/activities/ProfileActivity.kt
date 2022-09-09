package com.example.appbookremakekotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.appbookremakekotlin.MyApplication
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.adapters.AdapterPdfFavorites
import com.example.appbookremakekotlin.adapters.PdfAdminAdapter
import com.example.appbookremakekotlin.databinding.ActivityProfileBinding
import com.example.appbookremakekotlin.model.PDFModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var booksArrayList: ArrayList<PDFModel>

    private lateinit var adapterPdfFavorites: AdapterPdfFavorites

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadBookFavorites()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ProfileEditActivity::class.java))
        }

    }

    private fun loadBookFavorites() {

        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()
                    for(ds in snapshot.children) {
                        val bookId = "${ds.child("bookId").value}"

                        val modelPdf = PDFModel()
                        modelPdf.id = bookId

                        booksArrayList.add(modelPdf)
                    }

                    binding.favoriteBookTypeTv.text = "${booksArrayList.size}"

                    adapterPdfFavorites = AdapterPdfFavorites(this@ProfileActivity, booksArrayList)
                    binding.bookRv.adapter = adapterPdfFavorites
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val email = "${snapshot.child("email").value.toString()}"
                    val name = "${snapshot.child("name").value.toString()}"
                    val profileImage = "${snapshot.child("profileImage").value.toString()}"
                    val timestamp = "${snapshot.child("timestamp").value.toString()}"
                    val uid = "${snapshot.child("uid").value.toString()}"
                    val userType = "${snapshot.child("userType").value.toString()}"

                    val formatDate = MyApplication.formatTimestamp(timestamp.toLong())

                    binding.fullnameTv.text = name
                    binding.emailTv.text = email
                    binding.memberTypeTv.text = formatDate
                    binding.accountTypeTv.text = userType

                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileTv)
                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


}