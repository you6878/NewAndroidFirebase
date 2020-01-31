package com.howlab.newfirebase

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_storage.*
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class StorageActivity : AppCompatActivity() {
    val GALLERY = 0
    val FILE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)
        upload_file.setOnClickListener { openContent() }
        upload_photo.setOnClickListener { openAlbum() }
        delete_photo.setOnClickListener { deletePhoto() }
        load_photo.setOnClickListener {
            var storageRef = FirebaseStorage.getInstance().reference.child("images").child(load_filename_edittext.text.toString())
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                loadImage(uri.toString())
            }

        }
        download_photo.setOnClickListener {
            var storageRef = FirebaseStorage.getInstance().reference.child("images").child(download_filename_edittext.text.toString())
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                DownloadFileFromURL().execute(uri.toString())
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }
    fun openContent(){
        var intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, FILE)
    }

    fun uploadFile(fileUri: Uri) {
        var metaCursor = contentResolver.query(fileUri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),null,null,null)!!
        metaCursor.moveToFirst()
        var fileName = metaCursor.getString(0)
        metaCursor.close()

        var storageRef = FirebaseStorage.getInstance().reference.child("files").child(fileName)

        storageRef.putFile(fileUri).addOnSuccessListener {
            Toast.makeText(this, "Upload photo completed", Toast.LENGTH_LONG).show()
        }
    }

    fun openAlbum() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY)
    }

    fun uploadPhoto(photoUri: Uri) {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var fileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = FirebaseStorage.getInstance().reference.child("images").child(fileName)

        storageRef.putFile(photoUri).addOnSuccessListener {
            Toast.makeText(this, "Upload photo completed", Toast.LENGTH_LONG).show()
        }
    }

    fun deletePhoto() {
        FirebaseStorage.getInstance().reference.child("images")
            .child(delete_filename_edittext.text.toString()).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Delete photo completed", Toast.LENGTH_LONG).show()
            }
    }

    fun loadImage(downloadUrl : String) {
        Glide.with(this).load(downloadUrl).into(album_imageview)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY) {
            var photoUri = data?.data!!
            album_imageview.setImageURI(photoUri)
            uploadPhoto(photoUri)
        }else if(requestCode == FILE){
            var fileUri = data?.data!!
            uploadFile(fileUri)
        }
    }

   inner class DownloadFileFromURL : AsyncTask<String?, String?, String?>() {

        override fun doInBackground(vararg p0: String?): String? {
            //file download path
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()

            //image download url
            val url = URL(p0[0])
            val conection = url.openConnection()
            conection.connect()

            // input stream to read file - with 8k buffer
            val input = BufferedInputStream(url.openStream(), 8192)

            // output stream to write file
            val output = FileOutputStream(downloadFolder + "/howl_Dfile.jpg")
            val data = ByteArray(1024)
            var total = 0L

            // writing data to file
            var count : Int
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()

                output.write(data, 0, count)
            }
            // flushing output
            output.flush()
            // closing streams
            output.close()
            input.close()

            return null
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Toast.makeText(this@StorageActivity,"download file completed",Toast.LENGTH_LONG).show()
        }
    }
}

