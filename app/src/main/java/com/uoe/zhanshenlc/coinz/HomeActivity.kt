package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeActivity : AppCompatActivity() {

    lateinit var btn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btn = findViewById(R.id.button)

        btn.setOnClickListener {
            val register = Intent(this@HomeActivity, RegisterActivity::class.java)
            startActivity(register)
            finish()
        }
    }


}
