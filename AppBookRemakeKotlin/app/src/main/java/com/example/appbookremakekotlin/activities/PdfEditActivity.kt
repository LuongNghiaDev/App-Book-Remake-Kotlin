package com.example.appbookremakekotlin.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.databinding.ActivityPdfEditBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfEditBinding
    private var bookId = ""

    private lateinit var categoryTitleArrayList: ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.updateBtn.setOnClickListener {

            validateData()
        }

        loadCategories()
        loadBookInfo()
    }

    private fun loadBookInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    selectCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val category = snapshot.child("category").value.toString()

                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@PdfEditActivity, "Faild load pdf...", Toast.LENGTH_LONG).show()
                }

            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        if(title.isEmpty()) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_LONG).show()
        } else if(description.isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_LONG).show()
        } else if(selectCategoryId.isEmpty()) {
            Toast.makeText(this, "Pick Category..", Toast.LENGTH_LONG).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        progressDialog.setMessage("Update pdf info...")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("title", "$title")
        hashMap.put("description", "$description")
        hashMap.put("categoryId", "$selectCategoryId")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Update successfully PDF..", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed update PDF info...", Toast.LENGTH_LONG).show()
            }
    }

    private var selectCategoryId = ""
    private var selectCategoryTitle = ""

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)

        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, position ->

                selectCategoryId = categoryIdArrayList[position]
                selectCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectCategoryTitle

            }
            .show()

    }

    private fun loadCategories() {

        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id =""+ds.child("id").value
                    val category = ""+ds.child("category").value

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}