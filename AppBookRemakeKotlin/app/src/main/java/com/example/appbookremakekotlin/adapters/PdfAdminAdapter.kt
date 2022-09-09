package com.example.appbookremakekotlin.adapters

import android.app.AlertDialog
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
import com.example.appbookremakekotlin.activities.PdfEditActivity
import com.example.appbookremakekotlin.databinding.RowPdfAdminBinding
import com.example.appbookremakekotlin.model.FilterCategory
import com.example.appbookremakekotlin.model.FilterPDF
import com.example.appbookremakekotlin.model.PDFModel

class PdfAdminAdapter: RecyclerView.Adapter<PdfAdminAdapter.Myholder>, Filterable {

    private var context: Context
    public var pdfList: ArrayList<PDFModel>
    private val filterList: ArrayList<PDFModel>

    private var filter: FilterPDF? = null

    private lateinit var binding: RowPdfAdminBinding

    constructor(context: Context, pdfList: ArrayList<PDFModel>) : super() {
        this.context = context
        this.pdfList = pdfList
        this.filterList = pdfList
    }

    inner class Myholder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val btnMore = binding.moreBtn

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return Myholder(binding.root)
    }

    override fun onBindViewHolder(holder: Myholder, position: Int) {
        val model = pdfList[position]

        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        val formatDate = MyApplication.formatTimestamp(timestamp)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formatDate

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        MyApplication.loadPdfUrlSingle(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        holder.btnMore.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: PDFModel, holder: PdfAdminAdapter.Myholder) {
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        val options = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Options")
            .setItems(options) { dialog, position ->
                if(position == 0) {
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                } else if(position == 1) {
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = FilterPDF(filterList, this)
        }
        return filter as FilterPDF
    }
}