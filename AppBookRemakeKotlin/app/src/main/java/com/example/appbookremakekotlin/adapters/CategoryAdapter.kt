package com.example.appbookremakekotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.appbookremakekotlin.activities.PdfListAdminActivity
import com.example.appbookremakekotlin.databinding.RowCategoryBinding
import com.example.appbookremakekotlin.model.CategoryModel
import com.example.appbookremakekotlin.model.FilterCategory
import com.google.firebase.database.FirebaseDatabase

class CategoryAdapter: RecyclerView.Adapter<CategoryAdapter.MyHolder>, Filterable {

    private val context: Context
    public var categoryList: ArrayList<CategoryModel>
    private var filterList: ArrayList<CategoryModel>

    private var filter: FilterCategory? = null


    private lateinit var binding: RowCategoryBinding

    constructor(context: Context, categoryList: ArrayList<CategoryModel>) : super() {
        this.context = context
        this.categoryList = categoryList
        this.filterList = categoryList
    }

    inner class MyHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var categoryTv: TextView = binding.categoryTv
        var btnDelete: ImageButton = binding.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val model = categoryList[position]
        val id = model.id
        val catedory = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        holder.categoryTv.text = catedory

        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are sure you want to delete this category?")
                .setPositiveButton("Confirm") { a, d ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_LONG).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", catedory)
            context.startActivity(intent)
        }

    }

    private fun deleteCategory(model: CategoryModel, holder: CategoryAdapter.MyHolder) {
        val id = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }

}