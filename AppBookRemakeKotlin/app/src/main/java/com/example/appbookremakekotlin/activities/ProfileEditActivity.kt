package com.example.appbookremakekotlin.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.appbookremakekotlin.MyApplication
import com.example.appbookremakekotlin.R
import com.example.appbookremakekotlin.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileTv.setOnClickListener {
            showImageAttachMenu()
        }

        binding.updateBtn.setOnClickListener {

            validateData()
        }
    }

    private var name = ""

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()

        if(title.isEmpty()) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_LONG).show()
        } else {
            if(imageUri == null) {
                updateProfile("")
            } else {
                updateImage()
            }
        }
    }

    private fun updateImage() {
        progressDialog.setMessage("Upload profile image...")
        progressDialog.show()

        val filePathAndName = "ProfileImages/${firebaseAuth.uid}"

        val firebaseStorage = FirebaseStorage.getInstance().reference.child(filePathAndName)
        firebaseStorage.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadingImageUrl = "${uriTask.result}"

                updateProfile(uploadingImageUrl)

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed upload PDF..${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateProfile(uploadingImage: String) {
        progressDialog.setMessage("Upload profile...")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("name", "$name")
        if(imageUri != null) {
            hashMap.put("profileImage", uploadingImage)
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Upload successfully..", Toast.LENGTH_LONG).show()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed uploading info...", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value.toString()}"
                    val profileImage = "${snapshot.child("profileImage").value.toString()}"
                    val timestamp = "${snapshot.child("timestamp").value.toString()}"

                    binding.nameEt.setText(name)

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileTv)
                    } catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun showImageAttachMenu() {

        val popupMenu = PopupMenu(this, binding.profileTv)
        popupMenu.menu.add(Menu.NONE, 0,0,"Camera")
        popupMenu.menu.add(Menu.NONE, 1,1, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId

            if(id == 0) {
                pickImageCamera()
            } else if(id == 1) {
                pickImageGallery()
            }

            true
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauch.launch(intent)
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauch.launch(intent)
    }

    private val cameraActivityResultLauch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                //imageUri = data!!.data

                binding.profileTv.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }
    )

    private val galleryActivityResultLauch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                binding.profileTv.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }
    )

}