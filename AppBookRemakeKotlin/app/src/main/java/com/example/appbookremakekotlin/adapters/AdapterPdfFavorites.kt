package com.example.appbookremakekotlin.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appbookremakekotlin.MyApplication
import com.example.appbookremakekotlin.activities.PdfDetailActivity
import com.example.appbookremakekotlin.databinding.RowPdfProfileBinding
import com.example.appbookremakekotlin.model.PDFModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorites: RecyclerView.Adapter<AdapterPdfFavorites.Myholder> {

    private val context: Context
    private val booksArrayList: ArrayList<PDFModel>

    constructor(context: Context, booksArrayList: ArrayList<PDFModel>) : super() {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    private lateinit var binding: RowPdfProfileBinding

    inner class Myholder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val btnRemoveFavorite = binding.removeFavoriteBtn
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterPdfFavorites.Myholder {
        binding = RowPdfProfileBinding.inflate(LayoutInflater.from(context), parent, false)
        return Myholder(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterPdfFavorites.Myholder, position: Int) {

        val model = booksArrayList[position]

        loadBookDetails(model, holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        holder.btnRemoveFavorite.setOnClickListener {
            MyApplication.removeFromFavourite(context, model.id)
        }
    }

    private fun loadBookDetails(model: PDFModel, holder: AdapterPdfFavorites.Myholder) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value.toString()}"
                    val description = "${snapshot.child("description").value.toString()}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value.toString()}"
                    val timestamp = "${snapshot.child("timestamp").value.toString()}"
                    val title = "${snapshot.child("title").value.toString()}"
                    val uid = "${snapshot.child("uid").value.toString()}"
                    val url = "${snapshot.child("url").value.toString()}"
                    val viewsCount = "${snapshot.child("viewsCount").value.toString()}"

                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.downloadsCount = downloadsCount.toLong()
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.viewsCount = viewsCount.toLong()
                    model.categoryId = categoryId
                    model.url = url

                    val date = MyApplication.formatTimestamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, holder.categoryTv)

                    MyApplication.loadPdfUrlSingle("$url","$title", holder.pdfView, holder.progressBar,
                        null)

                    MyApplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }


}