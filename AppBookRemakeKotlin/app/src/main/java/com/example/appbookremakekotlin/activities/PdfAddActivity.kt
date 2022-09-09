package com.example.appbookremakekotlin.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.appbookremakekotlin.databinding.ActivityPdfAddBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryList: ArrayList<CategoryModel>
    private lateinit var progressDialog: ProgressDialog

    private var pdfUri: Uri? = null
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadPdfCategories()

        binding.categoryTv.setOnClickListener {
            categoryPickDataDialog()
        }

        binding.btnPdf.setOnClickListener {
            pdfPickIntent()
        }

        binding.btnUpload.setOnClickListener {

            validateData()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }



    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {

        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if(title.isEmpty()) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_LONG).show()
        } else if(description.isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_LONG).show()
        } else if(category.isEmpty()) {
            Toast.makeText(this, "Pick Category..", Toast.LENGTH_LONG).show()
        } else if(pdfUri == null) {
            Toast.makeText(this, "Pick PDF..", Toast.LENGTH_LONG).show()
        } else {
            uploadPdfToStorage()
        }

    }

    private fun uploadPdfToStorage() {

        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Books/$timestamp"

        val firebaseStorage = FirebaseStorage.getInstance().reference.child(filePathAndName)
        firebaseStorage.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"

                uploadInfoPdfToDB(uploadPdfUrl, timestamp)

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Faild upload pdf: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed upload PDF..${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadInfoPdfToDB(uploadPdfUrl: String, timestamp: Long) {

        progressDialog.setMessage("Uploading pdf info...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("uid", "$uid")
        hashMap.put("id", "$timestamp")
        hashMap.put("title", "$title")
        hashMap.put("description", "$description")
        hashMap.put("categoryId", "$selectCategoryId")
        hashMap.put("url", "$uploadPdfUrl")
        hashMap.put("timestamp", timestamp)
        hashMap.put("viewsCount", 0)
        hashMap.put("downloadsCount", 0)

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Upload successfully PDF..", Toast.LENGTH_LONG).show()
                pdfUri = null
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed uploading PDF info...", Toast.LENGTH_LONG).show()
            }

    }

    private fun loadPdfCategories() {
        categoryList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(CategoryModel::class.java)
                    categoryList.add(model!!)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectCategoryId = ""
    private var selectCategoryTitle = ""

    private fun categoryPickDataDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryList.size)

        for (i in categoryList.indices) {
            categoriesArray[i] = categoryList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->

                selectCategoryId = categoryList[which].id
                selectCategoryTitle = categoryList[which].category

                binding.categoryTv.text = selectCategoryTitle

            }
            .show()
    }

    private fun pdfPickIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLaucher.launch(intent)
    }

    val pdfActivityResultLaucher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if(result.resultCode == RESULT_OK) {

                pdfUri = result.data!!.data
            } else {
                Toast.makeText(this, "Error pick pdf", Toast.LENGTH_LONG).show()
            }
        }
    )
}