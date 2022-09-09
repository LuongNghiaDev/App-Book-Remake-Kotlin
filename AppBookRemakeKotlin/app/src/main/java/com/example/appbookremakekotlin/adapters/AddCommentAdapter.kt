package com.example.appbookremakekotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appbookremakekotlin.MyApplication
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.databinding.RowCommentBinding
import com.example.appbookremakekotlin.model.CommentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class AddCommentAdapter: RecyclerView.Adapter<AddCommentAdapter.Myholder> {

    private val context: Context
    private val commentList: ArrayList<CommentModel>
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: RowCommentBinding

    constructor(context: Context, commentList: ArrayList<CommentModel>) : super() {
        this.context = context
        this.commentList = commentList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class Myholder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val profileTv = binding.profileTv
        val nameTv = binding.nameTv
        val dateTv = binding.dateTv
        val commentTv = binding.comment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return Myholder(binding.root)
    }

    override fun onBindViewHolder(holder: Myholder, position: Int) {

        val model = commentList[position]

        val bookId = model.bookId
        val uid = model.uid
        val timestamp = model.timestamp
        val comment = model.comment
        val id = model.id

        val date = MyApplication.formatTimestamp(timestamp.toLong())

        holder.dateTv.text = date
        holder.commentTv.text = comment

        loadUserDetail(model, holder)

        holder.itemView.setOnClickListener {
            if(firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {

                deleteCommentDialog(model, holder)
            }
        }

    }

    private fun deleteCommentDialog(model: CommentModel, holder: AddCommentAdapter.Myholder) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage("Are you sure delete comment")
            .setPositiveButton("Delete") { d, e ->

                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Successfully comment", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed remove comment", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancel") { c, e ->
                c.dismiss()
            }
            .show()
    }

    private fun loadUserDetail(model: CommentModel, holder: AddCommentAdapter.Myholder) {
        val uid = model.uid

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    holder.nameTv.text = name

                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileTv)
                    } catch (e:Exception) {
                        holder.profileTv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return commentList.size
    }


}