package com.example.appbookremakekotlin

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.appbookremakekotlin.utils.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        fun formatTimestamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    val bytes = storageMetadata.sizeBytes.toDouble()

                    val kb = bytes/1024
                    val mb = kb/1024
                    if(mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if( kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", mb)} Bytes"
                    }
                }
                .addOnFailureListener { e ->

                }
        }

        fun loadPdfUrlSingle(pdfUrl:String , pdfTitle: String, pdfView: PDFView, progressBar: ProgressBar,
        pageTv:TextView?) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { e ->
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onLoad { pages ->
                            progressBar.visibility = View.INVISIBLE

                            if(pageTv != null) {
                                pageTv.text = "$pages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e ->

                }
        }

        fun loadCategory(categoryId:String, categoryTv:TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val category = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun deleteBook(context: Context, bookId:String, bookUrl: String, bookTitle: String) {

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait...")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val firebaseStorage = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            firebaseStorage.delete()
                .addOnSuccessListener {

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully delete...", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Faild delete...", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Faild delete...", Toast.LENGTH_LONG).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                       var viewCount = "${snapshot.child("viewsCount").value.toString()}"

                        if(viewCount == "" && viewCount == "null") {
                            viewCount = "0"
                        }

                        val newViewCount = viewCount.toLong() + 1

                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        public fun removeFromFavourite(context: Context, bookId: String) {
            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Remove favourites", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed remove favourites", Toast.LENGTH_LONG).show()
                }
        }

    }
}