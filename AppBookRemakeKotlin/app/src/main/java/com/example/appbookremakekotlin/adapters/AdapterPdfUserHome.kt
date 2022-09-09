package com.example.appbookremakekotlin.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.appbookremakekotlin.MyApplication
import com.example.appbookremakekotlin.activities.PdfDetailActivity
import com.example.appbookremakekotlin.databinding.RowListbookHomeBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.example.appbookremakekotlin.model.FilterCategory
import com.example.appbookremakekotlin.model.PDFModel

class AdapterPdfUserHome:RecyclerView.Adapter<AdapterPdfUserHome.Myholder> {

    private var context: Context
    private var bookArrayList: ArrayList<PDFModel>

    private lateinit var binding: RowListbookHomeBinding

    constructor(context: Context, bookArrayList: ArrayList<PDFModel>) : super() {
        this.context = context
        this.bookArrayList = bookArrayList
    }

    inner class Myholder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTv = binding.titleTv
        val pdfViewImage = binding.pdfView
        val progressBar = binding.progressBar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
        binding = RowListbookHomeBinding.inflate(LayoutInflater.from(context), parent, false)
        return Myholder(binding.root)
    }

    override fun onBindViewHolder(holder: Myholder, position: Int) {
        val model = bookArrayList[position]

        val bookId = model.id
        val nameBook = model.title
        val url = model.url

        holder.nameTv.text = nameBook

        MyApplication.loadPdfUrlSingle(url, nameBook, holder.pdfViewImage, holder.progressBar,null)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

}