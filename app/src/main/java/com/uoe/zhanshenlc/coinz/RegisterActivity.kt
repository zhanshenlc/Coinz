package com.uoe.zhanshenlc.coinz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val tag = "RegisterActivity"

    private var profile : ImageView? = null
    private var imageUri : Uri? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_signup.setOnClickListener(this)
        link_login.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.btn_signup -> createAccount(input_email.text.toString(), input_password.text.toString())
            R.id.link_login -> {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d(tag, "createAccount: $email")
        if (!validateForm()) {
            return
        }
        // [START create_user_with_email]
        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(tag, "createUserWithEmail:success")
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Log.w(tag, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, task.exception.toString(), Toast.LENGTH_LONG).show()
                startActivity(Intent(applicationContext, RegisterActivity::class.java))
                finish()
            }
        }
        // [END create_user_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val name = input_name.text.toString()
        if (TextUtils.isEmpty(name)) {
            input_name.error = "Required."
            valid = false
        } else { input_name.error = null }

        val email = input_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            input_email.error = "Required."
            valid = false
        } else { input_email.error = null }

        val password = input_password.text.toString()
        if (TextUtils.isEmpty(password)) {
            input_password.error = "Required."
            valid = false
        } else { input_password.error = null }

        return valid
    }

    private fun upload() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
        startActivityForResult(intent, 10)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            profile?.setImageURI(data!!.data)
            if (data != null) {
                imageUri = data.data
            }
        }
    }

}
