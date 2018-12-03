package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.CurrencyRates
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class BankActivity : AppCompatActivity() {

    private val tag = "BankActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())
    private var currencyChosen: String = ""

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
        findViewById<ImageButton>(R.id.quidBtn_bank).setOnClickListener {
            currencyChosen = "QUID"
            fireStore.collection("users").document(mAuth.uid.toString())
                    .collection("coins").document("bankAccount").get()
                    .addOnSuccessListener {
                        var quid = it.data!!["quid"] as Double
                        var gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "QUID"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = quid.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "QUID to GOLD"
                        fromGoldSwitch.text = "GOLD to QUID"
                        findViewById<Button>(R.id.exchangeBtn_bank).setOnClickListener {
                            var numeric: Boolean
                            var amount = 0.0
                            try {
                                amount = parseDouble(findViewById<EditText>(R.id.amount_bank).text.toString())
                                numeric = true
                            } catch (e: NumberFormatException) {
                                numeric = false
                                Toast.makeText(this, "Please input a number", Toast.LENGTH_SHORT).show()
                            }
                            if (numeric) {
                                when(toGoldSwitch.isChecked) {
                                    true -> {
                                        if (amount > quid * 0.95) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            quid -= amount
                                            gold += amount * 0.95 * parseDouble(findViewById<TextView>(R.id.quidRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["quid"] = quid
                                            result["gold"] = gold
                                            fireStore.collection("users").document(mAuth.uid.toString())
                                                    .collection("coins").document("bankAccount")
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = quid.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                    false -> {
                                        if (amount > gold * 0.95) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            gold -= amount
                                            quid += amount * 0.95 / parseDouble(findViewById<TextView>(R.id.quidRate_bank).text.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }
        }
        findViewById<ImageButton>(R.id.dolrBtn_bank).setOnClickListener {
            currencyChosen = "DOLR"
            fireStore.collection("users").document(mAuth.uid.toString())
                    .collection("coins").document("bankAccount").get()
                    .addOnSuccessListener {
                        val quid = it.data!!["quid"] as Double
                        val shil = it.data!!["shil"] as Double
                        val dolr = it.data!!["dolr"] as Double
                        val peny = it.data!!["peny"] as Double
                        val gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "DOLR"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = dolr.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "DOLR to GOLD"
                        fromGoldSwitch.text = "GOLD to DOLR"
                    }
        }
        findViewById<ImageButton>(R.id.shilBtn_bank).setOnClickListener {
            currencyChosen = "SHIL"
            fireStore.collection("users").document(mAuth.uid.toString())
                    .collection("coins").document("bankAccount").get()
                    .addOnSuccessListener {
                        val quid = it.data!!["quid"] as Double
                        val shil = it.data!!["shil"] as Double
                        val dolr = it.data!!["dolr"] as Double
                        val peny = it.data!!["peny"] as Double
                        val gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "SHIL"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = shil.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "SHIL to GOLD"
                        fromGoldSwitch.text = "GOLD to SHIL"
                    }
        }
        findViewById<ImageButton>(R.id.penyBtn_bank).setOnClickListener {
            currencyChosen = "PENY"
            fireStore.collection("users").document(mAuth.uid.toString())
                    .collection("coins").document("bankAccount").get()
                    .addOnSuccessListener {
                        val quid = it.data!!["quid"] as Double
                        val shil = it.data!!["shil"] as Double
                        val dolr = it.data!!["dolr"] as Double
                        val peny = it.data!!["peny"] as Double
                        val gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "PENY"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = peny.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "PENY to GOLD"
                        fromGoldSwitch.text = "GOLD to PENY"
                    }
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
