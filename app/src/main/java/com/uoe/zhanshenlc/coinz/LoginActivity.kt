package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    private val tag = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener(this)
        link_signup.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i) {
            R.id.btn_login -> signIn(input_email.text.toString(), input_password.text.toString())
            R.id.link_signup -> {
                startActivity(Intent(applicationContext, RegisterActivity::class.java))
                finish()
            }
        }
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
                Log.d(tag, "signInWithEmail:success")
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
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

}
