package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Double.parseDouble

class RateHistoryActivity : AppCompatActivity() {

    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_history)

        val toolbar: Toolbar = findViewById(R.id.toolbar_rateHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val year = findViewById<EditText>(R.id.inputYear_rateHistory)
        val month = findViewById<EditText>(R.id.inputMonth_rateHistory)
        val day = findViewById<EditText>(R.id.inputDay_rateHistory)
        findViewById<Button>(R.id.searchBtn_rateHistory).setOnClickListener {
            when {
                year.text.isEmpty() -> year.error = "Required."
                month.text.isEmpty() -> month.error = "Required."
                day.text.isEmpty() -> day.error = "Required."
                year.text.length != 4 -> year.error = "4 digits required."
                month.text.length != 2 -> month.error = "2 digits required."
                day.text.length != 2 -> day.error = "2 digits required."
                else -> {
                    var yyyy = 0
                    var mm = 0
                    var dd = 0
                    try {
                        yyyy = parseDouble(year.text.toString()).toInt()
                    } catch (e: NumberFormatException) {
                        year.error = "Please input a valid year."
                    }
                    try {
                        mm = parseDouble(month.text.toString()).toInt()
                    } catch (e: NumberFormatException) {
                        month.error = "Please input a valid year."
                    }
                    try {
                        dd = parseDouble(day.text.toString()).toInt()
                    } catch (e: NumberFormatException) {
                        day.error = "Please input a valid year."
                    }
                    if (yyyy != 0 && mm != 0 && dd != 0) {
                        val y = year.text.toString()
                        val m = month.text.toString()
                        val d = day.text.toString()
                        println("???$y$m$d")
                        fireStore.collection("exchange rate")
                                .document("$y$m$d").get()
                                .addOnSuccessListener {
                                    if (it.data != null) {
                                        val dolr = it.data!!["dolrToGold"] as Double
                                        val shil = it.data!!["shilToGold"] as Double
                                        val quid = it.data!!["quidToGold"] as Double
                                        val peny = it.data!!["penyToGold"] as Double
                                        findViewById<TextView>(R.id.dolrRate_rateHistory).text = dolr.toString()
                                        findViewById<TextView>(R.id.shilRate_rateHistory).text = shil.toString()
                                        findViewById<TextView>(R.id.quidRate_rateHistory).text = quid.toString()
                                        findViewById<TextView>(R.id.penyRate_rateHistory).text = peny.toString()
                                    } else {
                                        Toast.makeText(this, "No data available on this date.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                    }
                }
            }
        }
    }
}
