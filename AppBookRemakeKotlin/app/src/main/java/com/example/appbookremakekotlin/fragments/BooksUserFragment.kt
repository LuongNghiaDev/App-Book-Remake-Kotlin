package com.example.appbookremakekotlin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.adapters.PdfUserAdapter
import com.example.appbookremakekotlin.databinding.FragmentBooksUserBinding
import com.example.appbookremakekotlin.model.PDFModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    companion object {

        public fun newInstance(categoryId:String, category: String, uid:String): BooksUserFragment {
            val fragment = BooksUserFragment()

            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)

            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<PDFModel>
    private lateinit var adapterPdfUser: PdfUserAdapter

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments

        if(args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        if(category == "All") {
            loadAllBooks()
        } else if(category == "Most Viewed") {
            loadMostViewedDownloadBooks("viewsCount")
        } else if(category == "Most Downloaded") {
            loadMostViewedDownloadBooks("downloadsCount")
        } else {
            loadCategoriedBooks()
        }

        /*binding.searchEt.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(p0)
                } catch (e:Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })*/

        return binding.root
    }

    private fun loadMostViewedDownloadBooks(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()

                for(ds in snapshot.children) {
                    val model = ds.getValue(PDFModel::class.java)

                    pdfArrayList.add(model!!)
                }

                adapterPdfUser = PdfUserAdapter(context!!, pdfArrayList)
                binding.bookRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()

                for(ds in snapshot.children) {
                    val model = ds.getValue(PDFModel::class.java)

                    pdfArrayList.add(model!!)
                }

                adapterPdfUser = PdfUserAdapter(context!!, pdfArrayList)
                binding.bookRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadCategoriedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()

                for(ds in snapshot.children) {
                    val model = ds.getValue(PDFModel::class.java)

                    pdfArrayList.add(model!!)
                }

                adapterPdfUser = PdfUserAdapter(context!!, pdfArrayList)
                binding.bookRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


}