package com.example.appbookremakekotlin.model

import android.widget.Filter
import com.example.appbookremakekotlin.adapters.PdfAdminAdapter

class FilterPDF: Filter {

    var filterList: ArrayList<PDFModel>
    var adapterPdfAdmin: PdfAdminAdapter

    constructor(filterList: ArrayList<PDFModel>, adapterPdfAdmin: PdfAdminAdapter) : super() {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(p0: CharSequence?): FilterResults {
        var constraint: CharSequence? = p0

        val results = FilterResults()
        if(constraint != null && constraint.isNotEmpty()) {

            constraint = constraint.toString().lowercase()
            val filteredModels: ArrayList<PDFModel> = ArrayList()
            for (i in 0 until filterList.size) {

                if(filterList[i].title.lowercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results //don't miss it
    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
        adapterPdfAdmin.pdfList = p1?.values as ArrayList<PDFModel>

        adapterPdfAdmin.notifyDataSetChanged()
    }


}