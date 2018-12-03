package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.CurrencyRates
import java.text.SimpleDateFormat
import java.util.*

class BankActivity : AppCompatActivity() {

    private val tag = "BankActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        fireStore.collection("exchange rate").document("latest").get()
                .addOnSuccessListener {
                    val date = it.data!!["date"] as String
                    val dolr: Double
                    val shil: Double
                    val quid: Double
                    val peny: Double
                    val rates: CurrencyRates
                    if (date == today) {
                        Log.d(tag, "Exchange rate generated")
                        dolr = it.data!!["dolrToGold"] as Double
                        shil = it.data!!["shilToGold"] as Double
                        quid = it.data!!["quidToGold"] as Double
                        peny = it.data!!["penyToGold"] as Double
                        rates = CurrencyRates(today, dolr, quid, shil, peny)
                    } else {
                        rates = CurrencyRates(today)
                        dolr = rates.dolrToGold
                        shil = rates.shilToGold
                        quid = rates.quidToGold
                        peny = rates.penyToGold
                    }
                    fireStore.collection("exchange rate").document("latest")
                            .set(rates.toMap())
                            .addOnSuccessListener {  }
                            .addOnFailureListener {  }
                    findViewById<TextView>(R.id.dolrRate_bank).text = dolr.toString()
                    findViewById<TextView>(R.id.quidRate_bank).text = quid.toString()
                    findViewById<TextView>(R.id.shilRate_bank).text = shil.toString()
                    findViewById<TextView>(R.id.penyRate_bank).text = peny.toString()
                }

        val toGoldSwitch = findViewById<Switch>(R.id.toGoldSwitch_bank)
        val fromGoldSwitch = findViewById<Switch>(R.id.fromGoldSwitch_bank)
        toGoldSwitch.setOnClickListener {
            fromGoldSwitch.isChecked = ! fromGoldSwitch.isChecked
        }
        fromGoldSwitch.setOnClickListener{
            toGoldSwitch.isChecked = ! toGoldSwitch.isChecked
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar_bank)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
