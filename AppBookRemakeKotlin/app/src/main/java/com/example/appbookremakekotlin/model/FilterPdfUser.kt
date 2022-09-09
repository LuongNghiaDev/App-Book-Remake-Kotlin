package com.example.appbookremakekotlin.model

import android.widget.Filter
import com.example.appbookremakekotlin.adapters.PdfUserAdapter

class FilterPdfUser:Filter {

    private var filterListUser: ArrayList<PDFModel>
    private var adapterPdfUser: PdfUserAdapter

    constructor(filterListUser: ArrayList<PDFModel>, adapterPdfUser: PdfUserAdapter) : super() {
        this.filterListUser = filterListUser
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if(constraint != null && constraint.isNotEmpty()) {

            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<PDFModel> = ArrayList()
            for (i in filterListUser.indices) {
                if(filterListUser[i].title.uppercase().contains(constraint)) {
                    filteredModels.add(filterListUser[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterListUser.size
            results.values = filterListUser
        }
        return results
    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
        adapterPdfUser.pdfArrayList = p1?.values as ArrayList<PDFModel>

        adapterPdfUser.notifyDataSetChanged()
    }


}