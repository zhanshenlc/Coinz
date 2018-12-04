package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WalletActivity : AppCompatActivity() {

    private val tag = "WalletActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        fireStore.collection("bank accounts").document(mAuth.uid.toString())
        /*fireStore.collection("users").document(mAuth.uid.toString())
                .collection("coins").document("bankAccount")*/.get()
                .addOnSuccessListener {
                    val quid = it.data!!["quid"] as Double
                    val shil = it.data!!["shil"] as Double
                    val dolr = it.data!!["dolr"] as Double
                    val peny = it.data!!["peny"] as Double
                    val gold = it.data!!["gold"] as Double
                    findViewById<TextView>(R.id.quidAmount_wallet).text = quid.toString()
                    findViewById<TextView>(R.id.shilAmount_wallet).text = shil.toString()
                    findViewById<TextView>(R.id.dolrAmount_wallet).text = dolr.toString()
                    findViewById<TextView>(R.id.penyAmount_wallet).text = peny.toString()
                    findViewById<TextView>(R.id.goldAmount_wallet).text = gold.toString()
                }
                .addOnFailureListener {  }

        val toolbar: Toolbar = findViewById(R.id.toolbar_wallet)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
