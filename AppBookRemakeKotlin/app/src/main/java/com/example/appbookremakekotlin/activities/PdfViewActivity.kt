package com.example.appbookremakekotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.appbookremakekotlin.databinding.ActivityPdfViewBinding
import com.example.appbookremakekotlin.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding

    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        bookId = intent.getStringExtra("bookId")!!

        loadBookDetail()
    }

    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val pdfUrl = snapshot.child("url").value.toString()

                    loadBookFromUrl("$pdfUrl")

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->

                        val currenrPage = page+1
                        binding.toolbarSubTitleTv.text = "$currenrPage/$pageCount"

                    }
                    .onError { t ->

                    }
                    .onPageError { page, t ->

                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

}