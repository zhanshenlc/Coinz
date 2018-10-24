package com.uoe.zhanshenlc.coinz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import java.net.URI

class RegisterActivity : AppCompatActivity() {

    lateinit var id : EditText
    lateinit var password : EditText
    lateinit var nickname : EditText
    lateinit var profile : ImageView
    lateinit var btn : Button
    lateinit var imageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        id = findViewById(R.id.id)
        password = findViewById(R.id.password)
        nickname = findViewById(R.id.nickname)
        profile = findViewById(R.id.img_profile)
        btn = findViewById(R.id.signup)

        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view : View) {
                signup()
            }
        })

        profile.setOnClickListener {
            upload()
        }

    }

    private fun signup() {

    }

    private fun upload() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
        startActivityForResult(intent, 10)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            profile.setImageURI(data?.data)
            if (data != null) {
                imageUri = data.data
            }
        }
    }

}
