package com.example.appbookremakekotlin.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.appbookremakekotlin.activities.PdfListAdminActivity
import com.example.appbookremakekotlin.databinding.CategoryItemBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.example.appbookremakekotlin.model.FilterCategory
import com.example.appbookremakekotlin.model.FilterCategoryHome

class AdapterCategoryHome: RecyclerView.Adapter<AdapterCategoryHome.Myholder>, Filterable{

    private val context: Context
    public var categoryListHome: ArrayList<CategoryModel>

    private lateinit var binding: CategoryItemBinding

    private var filterList: ArrayList<CategoryModel>

    private var filter: FilterCategoryHome? = null

    constructor(context: Context, categoryListHome: ArrayList<CategoryModel>) : super() {
        this.context = context
        this.categoryListHome = categoryListHome
        this.filterList = categoryListHome
    }

    inner class Myholder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name = binding.nameTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
        binding = CategoryItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Myholder(binding.root)
    }

    override fun onBindViewHolder(holder: Myholder, position: Int) {
        val model = categoryListHome[position]

        val category = model.category
        val id = model.id

        holder.name.text = category

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categoryListHome.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = FilterCategoryHome(filterList, this)
        }
        return filter as FilterCategoryHome
    }


}