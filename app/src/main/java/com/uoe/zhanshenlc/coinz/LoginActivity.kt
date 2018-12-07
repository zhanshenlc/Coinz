package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private val tag = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.btn_login).setOnClickListener{
            signIn(findViewById<EditText>(R.id.inputEmail_login).text.toString(),
                    findViewById<EditText>(R.id.inputPassword_login).text.toString())
        }
        findViewById<TextView>(R.id.signUpLink_login).setOnClickListener{
            finish()
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }
        findViewById<TextView>(R.id.forgetPassword_login).setOnClickListener {
            startActivity(Intent(applicationContext, ForgetPasswordActivity::class.java))
        }

        mAuth = FirebaseAuth.getInstance()
    }

    private fun signIn(email: String, password: String) {
        Log.d(tag, "signIn:$email")
        if (!validateForm()) {
            return
        }
        // [START sign_in_with_email]
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                if (mAuth?.currentUser!!.isEmailVerified) {
                    Log.d(tag, "signInWithEmail:success")
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                } else {
                    Log.d(tag, "signInWithEmail:failure")
                    Toast.makeText(this, "Please Verify Your Email.", Toast.LENGTH_LONG).show()
                    mAuth?.signOut()
                }
            } else {
                // If sign in fails, display a message to the user.
                Log.w(tag, "signInWithEmail:failure", task.exception)
                Toast.makeText(baseContext, task.exception.toString(), Toast.LENGTH_LONG).show()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
        }
        // [END sign_in_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true


        val email = findViewById<EditText>(R.id.inputEmail_login).text.toString()
        if (TextUtils.isEmpty(email)) {
            findViewById<EditText>(R.id.inputEmail_login).error = "Required."
            valid = false
        } else { findViewById<EditText>(R.id.inputEmail_login).error = null }

        val password = findViewById<EditText>(R.id.inputPassword_login).text.toString()
        if (TextUtils.isEmpty(password)) {
            findViewById<EditText>(R.id.inputPassword_login).error = "Required."
            valid = false
        } else { findViewById<EditText>(R.id.inputPassword_login).error = null }

        return valid
    }

}
