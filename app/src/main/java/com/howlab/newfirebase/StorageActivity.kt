package com.howlab.newfirebase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_storage.*
import java.text.SimpleDateFormat
import java.util.*

class StorageActivity : AppCompatActivity() {
    val GALLERY = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)
        upload_photo.setOnClickListener { openAlbum() }
        delete_photo.setOnClickListener { deletePhoto() }
    }
    fun openAlbum(){
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,GALLERY)
    }
    fun uploadPhoto(photoUri : Uri){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var fileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = FirebaseStorage.getInstance().reference.child("images").child(fileName)

        storageRef.putFile(photoUri).addOnSuccessListener {
            Toast.makeText(this,"Upload photo completed",Toast.LENGTH_LONG).show()
        }
    }
    fun deletePhoto(){
        FirebaseStorage.getInstance().reference.child("images").child(delete_filename_edittext.text.toString()).delete()
            .addOnSuccessListener {
                Toast.makeText(this,"Delete photo completed",Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALLERY){
            var photoUri = data?.data!!
            album_imageview.setImageURI(photoUri)
            uploadPhoto(photoUri)
        }
    }
}
