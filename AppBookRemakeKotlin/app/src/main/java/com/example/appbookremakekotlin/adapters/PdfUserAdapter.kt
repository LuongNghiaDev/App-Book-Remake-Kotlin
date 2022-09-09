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
import com.example.appbookremakekotlin.databinding.RowPdfUserBinding
import com.example.appbookremakekotlin.model.FilterPDF
import com.example.appbookremakekotlin.model.FilterPdfUser
import com.example.appbookremakekotlin.model.PDFModel

class PdfUserAdapter: RecyclerView.Adapter<PdfUserAdapter.MyHolder> ,Filterable{

    private var context:Context
    public var pdfArrayList: ArrayList<PDFModel>
    private lateinit var binding: RowPdfUserBinding
    private val filterListUser: ArrayList<PDFModel>

    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<PDFModel>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterListUser = pdfArrayList
    }

    inner class MyHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val model = pdfArrayList[position]

        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val url = model.url
        val uid = model.uid
        val timestamp = model.timestamp

        val formatDate = MyApplication.formatTimestamp(timestamp)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formatDate

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        MyApplication.loadPdfUrlSingle(url, title, holder.pdfView, holder.progressBar, null)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = FilterPdfUser(filterListUser, this)
        }
        return filter as FilterPdfUser
    }


}