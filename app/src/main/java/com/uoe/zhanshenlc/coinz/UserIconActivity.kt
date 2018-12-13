package com.uoe.zhanshenlc.coinz

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Math.min

class UserIconActivity : AppCompatActivity() {

    private val resultLoadImage = 1
    private val tag = "UserIconActivity"
    private val storage = FirebaseStorage.getInstance()
    private val email = FirebaseAuth.getInstance().currentUser?.email.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_icon)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_userIcon)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Choose image from gallery
        findViewById<ImageView>(R.id.image_userIcon).setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, resultLoadImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resultLoadImage && resultCode == Activity.RESULT_OK && data != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
            val width = bitmap.width
            val height = bitmap.height
            val length = min(width, height)
            // Crop user image as square (centered in the middle)
            val cropped = Bitmap.createBitmap(bitmap, (width - length) / 2, (height - length) / 2, length, length)
            findViewById<ImageView>(R.id.image_userIcon).setImageBitmap(cropped)
            val uploadBtn = findViewById<Button>(R.id.uploadBtn_userIcon)
            // If a valid picture is selected, the upload button becomes clickable
            uploadBtn.isClickable = true
            uploadBtn.setBackgroundResource(R.drawable.my_button2)
            // Upload to FireBase Storage
            uploadBtn.setOnClickListener {
                val iconRef = storage.reference.child("userIcons/$email.jpg")
                val baos = ByteArrayOutputStream()
                cropped.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                iconRef.putBytes(baos.toByteArray())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successfully set new icon", Toast.LENGTH_SHORT).show()
                            val iconFile = File(this.cacheDir, "iconTemp.jpg")
                            iconRef.getFile(iconFile)
                                    .addOnSuccessListener {
                                        Log.d(tag, "[getNewUserIcon] Success")
                                    }
                                    .addOnFailureListener { e -> Log.e(tag, "[getNewUserIcon] Failed with: $e") }
                            findViewById<Button>(R.id.uploadBtn_userIcon).isClickable = false
                            findViewById<Button>(R.id.uploadBtn_userIcon).setBackgroundColor(Color.GRAY)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed with: $e", Toast.LENGTH_SHORT).show()
                        }
            }
        }
    }

}
