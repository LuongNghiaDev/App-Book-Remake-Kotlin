package com.example.appbookremakekotlin.model

import android.widget.Filter
import com.example.appbookremakekotlin.adapters.CategoryAdapter

class FilterCategory: Filter {

    private var filterList: ArrayList<CategoryModel>
    private var adapterCategory: CategoryAdapter

    constructor(filterList: ArrayList<CategoryModel>, adapterCategory: CategoryAdapter) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(p0: CharSequence?): FilterResults {
        var constraint = p0
        val results = FilterResults()

        // value should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()) {
            //search value is nor null not empty

                //change to uppercase , or lower case to avoid case sensitivity
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<CategoryModel> = ArrayList()
            for (i in 0 until filterList.size) {

                if(filterList[i].category.uppercase().contains(constraint)) {
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
        adapterCategory.categoryList = p1?.values as ArrayList<CategoryModel>

        adapterCategory.notifyDataSetChanged()
    }
}