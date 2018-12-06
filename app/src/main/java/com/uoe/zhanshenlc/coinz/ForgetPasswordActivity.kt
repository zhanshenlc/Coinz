package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    private val tag = "ForgetPasswordActivity"
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        val toolbar: Toolbar = findViewById(R.id.toolbar_forgetPassword)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.sendBtn_forgetPassword).setOnClickListener{
            val emailView = findViewById<EditText>(R.id.inputEmail_forgetPwd)
            if (emailView.text.isEmpty()) {
                emailView.error = "Please input an email."
            } else {
                mAuth.sendPasswordResetEmail(emailView.text.toString())
                        .addOnSuccessListener { Log.d(tag, "Set data succeeded.") }
                        .addOnFailureListener { e -> Log.e(tag, "Set data failed with: $e") }
                Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
