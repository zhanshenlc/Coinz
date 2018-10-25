package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeActivity : AppCompatActivity() {

    lateinit var btn : Button
    lateinit var mAuth: FirebaseAuth
    internal var logged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btn = findViewById(R.id.button)

        btn.setOnClickListener {
            if (logged) {
                startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
            } else {
                startActivity(Intent(this@HomeActivity, RegisterActivity::class.java))
            }
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) { logged = true }
    }

}
