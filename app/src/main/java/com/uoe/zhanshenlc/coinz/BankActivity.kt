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

        fireStore.collection("exchange rate").document(today).get()
                .addOnSuccessListener {
                    val dolr: Double
                    val shil: Double
                    val quid: Double
                    val peny: Double
                    if (it.data == null) {
                        Log.d(tag, "Exchange rate generated")
                        val rates = CurrencyRates(today)
                        dolr = rates.dolrToGold
                        shil = rates.shilToGold
                        quid = rates.quidToGold
                        peny = rates.penyToGold
                        fireStore.collection("exchange rate").document(today)
                                .set(rates.toMap())
                                .addOnSuccessListener {  }
                                .addOnFailureListener {  }
                    } else {
                        dolr = it.data!!["dolrToGold"] as Double
                        shil = it.data!!["shilToGold"] as Double
                        quid = it.data!!["quidToGold"] as Double
                        peny = it.data!!["penyToGold"] as Double
                    }
                    findViewById<TextView>(R.id.dolrRate_bank).text = dolr.toString()
                    findViewById<TextView>(R.id.quidRate_bank).text = quid.toString()
                    findViewById<TextView>(R.id.shilRate_bank).text = shil.toString()
                    findViewById<TextView>(R.id.penyRate_bank).text = peny.toString()
                }
        /*fireStore.collection("exchange rate").document("latest").get()
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
                }*/

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
            fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
            /*fireStore.collection("users").document(mAuth.uid.toString())
                    .collection("coins").document("bankAccount").get()*/
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
                                        if (amount > quid) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            quid -= amount
                                            gold += amount * 0.95 * parseDouble(findViewById<TextView>(R.id.quidRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["quid"] = quid
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                            /*fireStore.collection("users").document(mAuth.uid.toString())
                                                    .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = quid.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                    false -> {
                                        if (amount > gold) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            gold -= amount
                                            quid += amount * 0.95 / parseDouble(findViewById<TextView>(R.id.quidRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["quid"] = quid
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = quid.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                }
                            }
                        }
                    }
        }
        findViewById<ImageButton>(R.id.dolrBtn_bank).setOnClickListener {
            currencyChosen = "DOLR"
            fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                    /*fireStore.collection("users").document(mAuth.uid.toString())
                            .collection("coins").document("bankAccount").get()*/
                    .addOnSuccessListener {
                        var dolr = it.data!!["dolr"] as Double
                        var gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "DOLR"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = dolr.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "DOLR to GOLD"
                        fromGoldSwitch.text = "GOLD to DOLR"
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
                                        if (amount > dolr) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            dolr -= amount
                                            gold += amount * 0.95 * parseDouble(findViewById<TextView>(R.id.dolrRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["dolr"] = dolr
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = dolr.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                    false -> {
                                        if (amount > gold) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            gold -= amount
                                            dolr += amount * 0.95 / parseDouble(findViewById<TextView>(R.id.dolrRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["dolr"] = dolr
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = dolr.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                }
                            }
                        }
                    }
        }
        findViewById<ImageButton>(R.id.shilBtn_bank).setOnClickListener {
            currencyChosen = "SHIL"
            fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                    /*fireStore.collection("users").document(mAuth.uid.toString())
                            .collection("coins").document("bankAccount").get()*/
                    .addOnSuccessListener {
                        var shil = it.data!!["shil"] as Double
                        var gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "SHIL"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = shil.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "SHIL to GOLD"
                        fromGoldSwitch.text = "GOLD to SHIL"
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
                                        if (amount > shil) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            shil -= amount
                                            gold += amount * 0.95 * parseDouble(findViewById<TextView>(R.id.shilRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["shil"] = shil
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = shil.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                    false -> {
                                        if (amount > gold) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            gold -= amount
                                            shil += amount * 0.95 / parseDouble(findViewById<TextView>(R.id.shilRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["shil"] = shil
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = shil.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                }
                            }
                        }
                    }
        }
        findViewById<ImageButton>(R.id.penyBtn_bank).setOnClickListener {
            currencyChosen = "PENY"
            fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                    /*fireStore.collection("users").document(mAuth.uid.toString())
                            .collection("coins").document("bankAccount").get()*/
                    .addOnSuccessListener {
                        var peny = it.data!!["peny"] as Double
                        var gold = it.data!!["gold"] as Double
                        findViewById<TextView>(R.id.currency_bank).text = "PENY"
                        findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                        findViewById<TextView>(R.id.currencyAmount_bank).text = peny.toString()
                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                        toGoldSwitch.text = "PENY to GOLD"
                        fromGoldSwitch.text = "GOLD to PENY"
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
                                        if (amount > peny) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            peny -= amount
                                            gold += amount * 0.95 * parseDouble(findViewById<TextView>(R.id.penyRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["dolr"] = peny
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = peny.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                    false -> {
                                        if (amount > gold) {
                                            Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                        } else {
                                            gold -= amount
                                            peny += amount * 0.95 / parseDouble(findViewById<TextView>(R.id.penyRate_bank).text.toString())
                                            val result = HashMap<String, Double>()
                                            result["peny"] = peny
                                            result["gold"] = gold
                                            fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                    /*fireStore.collection("users").document(mAuth.uid.toString())
                                                            .collection("coins").document("bankAccount")*/
                                                    .update(result.toMap())
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {  }
                                            findViewById<TextView>(R.id.currencyAmount_bank).text = peny.toString()
                                            findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        }
                                    }
                                }
                            }
                        }
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

    fun fuck(lowerCase: String, upperCase: String, rate: Int) {
        fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                /*fireStore.collection("users").document(mAuth.uid.toString())
                        .collection("coins").document("bankAccount").get()*/
                .addOnSuccessListener {
                    var currency = it.data!![lowerCase] as Double
                    var gold = it.data!!["gold"] as Double
                    findViewById<TextView>(R.id.currency_bank).text = upperCase
                    findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                    findViewById<TextView>(R.id.currencyAmount_bank).text = currency.toString()
                    findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                    val toGoldSwitch = findViewById<Switch>(R.id.toGoldSwitch_bank)
                    val fromGoldSwitch = findViewById<Switch>(R.id.fromGoldSwitch_bank)
                    toGoldSwitch.text = "$upperCase to GOLD"
                    fromGoldSwitch.text = "GOLD to $upperCase"
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
                                    if (amount > currency) {
                                        Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currency -= amount
                                        gold += amount * 0.95 * parseDouble(findViewById<TextView>(rate).text.toString())
                                        val result = HashMap<String, Double>()
                                        result[lowerCase] = currency
                                        result["gold"] = gold
                                        fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                /*fireStore.collection("users").document(mAuth.uid.toString())
                                                        .collection("coins").document("bankAccount")*/
                                                .update(result.toMap())
                                                .addOnSuccessListener {  }
                                                .addOnFailureListener {  }
                                        findViewById<TextView>(R.id.currencyAmount_bank).text = currency.toString()
                                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                    }
                                }
                                false -> {
                                    if (amount > gold) {
                                        Toast.makeText(this, "Please input a valid amount", Toast.LENGTH_SHORT).show()
                                    } else {
                                        gold -= amount
                                        currency += amount * 0.95 / parseDouble(findViewById<TextView>(rate).text.toString())
                                        val result = HashMap<String, Double>()
                                        result[lowerCase] = currency
                                        result["gold"] = gold
                                        fireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                /*fireStore.collection("users").document(mAuth.uid.toString())
                                                        .collection("coins").document("bankAccount")*/
                                                .update(result.toMap())
                                                .addOnSuccessListener {  }
                                                .addOnFailureListener {  }
                                        findViewById<TextView>(R.id.currencyAmount_bank).text = currency.toString()
                                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                    }
                                }
                            }
                        }
                    }
                }
    }

}
