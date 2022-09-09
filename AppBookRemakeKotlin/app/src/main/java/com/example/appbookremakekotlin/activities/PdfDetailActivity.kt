package com.example.appbookremakekotlin.activities

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.appbookremakekotlin.MyApplication
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.adapters.AddCommentAdapter
import com.example.appbookremakekotlin.databinding.ActivityPdfDetailBinding
import com.example.appbookremakekotlin.databinding.DialogCommentAddBinding
import com.example.appbookremakekotlin.model.CommentModel
import com.example.appbookremakekotlin.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""

    private var isInFavourites = false

    private lateinit var firebaseAuth:FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var commentArrayList: ArrayList<CommentModel>
    private lateinit var adapterComment: AddCommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.backbtn.setOnClickListener {
            onBackPressed()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        bookId = intent.getStringExtra("bookId")!!

        loadBookDetail()
        MyApplication.incrementBookViewCount(bookId)
        loadCommentDetail()

        binding.btnReadBook.setOnClickListener {
            val intent = Intent(this@PdfDetailActivity, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.btnDownloadBook.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadBook()
            } else {
                requestStoragePermissonLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if(firebaseAuth.currentUser != null) {
            checkIsFavourite()
        }

        binding.btnFavourite.setOnClickListener {
            if(firebaseAuth.currentUser == null) {

                Toast.makeText(this, "You're not logged in", Toast.LENGTH_LONG).show()
            }  else {

                if(isInFavourites) {

                    MyApplication.removeFromFavourite(this, bookId)
                } else {
                    addToFavourite()
                }
            }
        }

        binding.btnAddComments.setOnClickListener {
            if(firebaseAuth.currentUser == null) {
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_LONG).show()
            } else {
                addCommentDialog()
            }
        }

    }

    private fun loadCommentDetail() {
        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(CommentModel::class.java)

                        commentArrayList.add(model!!)
                    }

                    adapterComment = AddCommentAdapter(this@PdfDetailActivity, commentArrayList)
                    binding.commentRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var comment = ""

    private fun addCommentDialog() {

        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this,R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDilog = builder.create()
        alertDilog.show()

        commentAddBinding.btnBack.setOnClickListener { alertDilog.dismiss() }
        commentAddBinding.btnComment.setOnClickListener {
            comment = commentAddBinding.commentEt.text.toString()

            if(comment.isEmpty()) {
                Toast.makeText(this, "Enter comment...", Toast.LENGTH_LONG).show()
            } else {
                alertDilog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {

        progressDialog.setMessage("Add Comment...")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Add Comment Successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed Add Comment", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkIsFavourite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites").child(bookId)
            .addValueEventListener(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInFavourites = snapshot.exists()

                    if(isInFavourites) {
                        binding.btnFavourite.setImageResource(R.drawable.ic_favorite_white)
                        //binding.btnFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0)
                        //binding.btnFavourite.text = "Remove Favourite"
                    } else {
                        binding.btnFavourite.setImageResource(R.drawable.ic_favorite_border_white)
                        //binding.btnFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0)
                        //binding.btnFavourite.text = "Add Favourite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    private fun addToFavourite() {
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

                Toast.makeText(this, "Failed add to favourites", Toast.LENGTH_LONG).show()
            }
    }

    private val requestStoragePermissonLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            downloadBook()
        } else {
            Toast.makeText(this, "Permisson denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadBook() {
        progressDialog.setMessage("Downloading Book...")
        progressDialog.show()

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageRef.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->

                saveToDownloadFolder(bytes)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed download Book", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {

        val nameWithExtension = "${System.currentTimeMillis()}.pdf"

        try {

            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs()
            val filePath = downloadFolder.path +"/" + nameWithExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Saved to Downaload folder", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
            incrementDownLoadCount()

        } catch (e: Exception) {
            Toast.makeText(this, "Failed saved to Downaload folder", Toast.LENGTH_LONG).show()
        }
    }

    private fun incrementDownLoadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value.toString()}"

                    if(downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }
                    val newDownloadsCount:Long = downloadsCount.toLong() + 1

                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap.put("downloadsCount", newDownloadsCount)

                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {


                        }
                        .addOnFailureListener {

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val categoryId = "${snapshot.child("categoryId").value.toString()}"
                    val description = "${snapshot.child("description").value.toString()}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value.toString()}"
                    val timestamp = "${snapshot.child("timestamp").value.toString()}"
                    bookTitle = "${snapshot.child("title").value.toString()}"
                    val uid = "${snapshot.child("uid").value.toString()}"
                    bookUrl = "${snapshot.child("url").value.toString()}"
                    val viewsCount = "${snapshot.child("viewsCount").value.toString()}"

                    val date = MyApplication.formatTimestamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfUrlSingle("$bookUrl","$bookTitle", binding.pdfView, binding.progressBar,
                    binding.pagesTv)

                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadTv.text = downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}