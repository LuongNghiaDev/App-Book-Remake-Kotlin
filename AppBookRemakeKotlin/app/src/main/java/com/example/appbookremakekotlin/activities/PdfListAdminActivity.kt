package com.example.appbookremakekotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.adapters.PdfAdminAdapter
import com.example.appbookremakekotlin.databinding.ActivityPdfListAdminBinding
import com.example.appbookremakekotlin.model.PDFModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding
    private var categoryId = ""
    private var category = ""
    private lateinit var pdfArrayList: ArrayList<PDFModel>

    private lateinit var adapterPdfAdmin: PdfAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.subTitleTv.text = category

        loadPdfList()

        binding.backbtn.setOnClickListener {
            onBackPressed()
        }

        /*binding.searchEt.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfAdmin.filter!!.filter(p0)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })*/

    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for(ds in snapshot.children) {
                        val model = ds.getValue(PDFModel::class.java)

                        if(model != null) {
                            pdfArrayList.add(model!!)
                        }
                    }

                    adapterPdfAdmin = PdfAdminAdapter(this@PdfListAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}