package com.uoe.zhanshenlc.coinz

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.lang.Math.min

class UserIconActivity : AppCompatActivity() {

    private val resultLoadImage = 1
    private val storage = FirebaseStorage.getInstance()
    private val email = FirebaseAuth.getInstance().currentUser?.email.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_icon)

        val toolbar: Toolbar = findViewById(R.id.toolbar_userIcon)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

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
            val cropped = Bitmap.createBitmap(bitmap, (width - length) / 2, (height - length) / 2, length, length)
            findViewById<ImageView>(R.id.image_userIcon).setImageBitmap(cropped)
            findViewById<Button>(R.id.uploadBtn_userIcon).isClickable = true
            findViewById<Button>(R.id.uploadBtn_userIcon).setBackgroundResource(R.drawable.my_button2)

            val iconRef = storage.reference.child("userIcons/$email.jpg")
            val baos = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            iconRef.putBytes(baos.toByteArray())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Successfully set new icon", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed with: $e", Toast.LENGTH_SHORT).show()
                    }
        }
    }

}
