package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.uoe.zhanshenlc.coinz.dataModels.CurrencyRates
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap

class BankActivity : AppCompatActivity() {

    private val tag = "BankActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())
    private val todayNoSlashes = today.replace("/", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        val geoJson = BufferedReader(InputStreamReader(openFileInput("coinzmap.geojson")))
                .lines().collect(Collectors.joining(System.lineSeparator()))
        val json = JsonParser().parse(geoJson)
        val dateGenerated = json.asJsonObject["date-generated"].toString()
        val rates = Gson().fromJson(json.asJsonObject["rates"], HashMap::class.java)

        if (! CurrencyRates(today).dateCheck(dateGenerated)) {
            Toast.makeText(this, "Updating coins information. Please wait.", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(Intent(applicationContext, SplashActivity::class.java))
        } else {
            // Get exchange rates of the day
            // If no available data, then generate one set and upload
            fireStore.collection("exchange rate").document(todayNoSlashes).get()
                    .addOnSuccessListener {
                        Log.d(tag, "Reading data succeeded.")
                        val dolr: Double
                        val shil: Double
                        val quid: Double
                        val peny: Double
                        if (it.data == null) {
                            Log.d(tag, "Exchange rate generated")
                            dolr = rates["DOLR"].toString().toDouble()
                            shil = rates["SHIL"].toString().toDouble()
                            quid = rates["QUID"].toString().toDouble()
                            peny = rates["PENY"].toString().toDouble()
                            fireStore.collection("exchange rate").document(todayNoSlashes)
                                    .set(CurrencyRates(today, dolr, shil, quid, peny).toMap())
                                    .addOnSuccessListener { Log.d(tag, "Setting data succeeded.") }
                                    .addOnFailureListener { e -> Log.e(tag, "Setting data failed with: $e") }
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
                    .addOnFailureListener { e -> Log.e(tag, "Reading data failed with: $e") }

            val toGoldSwitch = findViewById<Switch>(R.id.toGoldSwitch_bank)
            val fromGoldSwitch = findViewById<Switch>(R.id.fromGoldSwitch_bank)
            toGoldSwitch.setOnClickListener {
                fromGoldSwitch.isChecked = !fromGoldSwitch.isChecked
            }
            fromGoldSwitch.setOnClickListener {
                toGoldSwitch.isChecked = !toGoldSwitch.isChecked
            }

            findViewById<ImageButton>(R.id.quidBtn_bank).setOnClickListener {
                exchange("quid", "QUID", R.id.quidRate_bank, toGoldSwitch, fromGoldSwitch)
            }
            findViewById<ImageButton>(R.id.shilBtn_bank).setOnClickListener {
                exchange("shil", "SHIL", R.id.shilRate_bank, toGoldSwitch, fromGoldSwitch)
            }
            findViewById<ImageButton>(R.id.dolrBtn_bank).setOnClickListener {
                exchange("dolr", "DOLR", R.id.dolrRate_bank, toGoldSwitch, fromGoldSwitch)
            }
            findViewById<ImageButton>(R.id.penyBtn_bank).setOnClickListener {
                exchange("peny", "PENY", R.id.penyRate_bank, toGoldSwitch, fromGoldSwitch)
            }

            findViewById<Button>(R.id.exchangeBtn_bank).isClickable = false

            val toolbar: Toolbar = findViewById(R.id.toolbar_bank)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            toolbar.setNavigationOnClickListener {
                finish()
            }

            findViewById<ImageButton>(R.id.rateHistory_bank).setOnClickListener {
                startActivity(Intent(applicationContext, RateHistoryActivity::class.java))
            }
        }
    }

    // 5% is charged for each exchange transaction
    // Colors of buttons and balances involved in transaction are changed to notify user of the change
    private fun exchange(lowerCase: String, upperCase: String, rate: Int, toGoldSwitch: Switch, fromGoldSwitch: Switch) {
        findViewById<Button>(R.id.exchangeBtn_bank).isClickable = true
        findViewById<Button>(R.id.exchangeBtn_bank).setBackgroundColor(Color.MAGENTA)
        fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                .addOnSuccessListener {
                    Log.d(tag, "Reading data succeeded.")
                    var currency = it.data!![lowerCase] as Double
                    var gold = it.data!!["gold"] as Double
                    findViewById<TextView>(R.id.currency_bank).text = upperCase
                    findViewById<TextView>(R.id.gold_bank).text = "GOLD"
                    findViewById<TextView>(R.id.currencyAmount_bank).text = currency.toString()
                    findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                    toGoldSwitch.text = "$upperCase to GOLD"
                    fromGoldSwitch.text = "GOLD to $upperCase"
                    toGoldSwitch.setOnClickListener {
                        fromGoldSwitch.isChecked = ! fromGoldSwitch.isChecked
                        when(lowerCase) {
                            "quid" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.YELLOW)
                            "shil" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.CYAN)
                            "dolr" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.GREEN)
                            "peny" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.MAGENTA)
                        }
                        findViewById<TextView>(R.id.goldAmount_bank).setTextColor(Color.WHITE)
                    }
                    fromGoldSwitch.setOnClickListener {
                        toGoldSwitch.isChecked = ! toGoldSwitch.isChecked
                        when(lowerCase) {
                            "quid" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.YELLOW)
                            "shil" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.CYAN)
                            "dolr" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.GREEN)
                            "peny" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.MAGENTA)
                        }
                        findViewById<TextView>(R.id.goldAmount_bank).setTextColor(Color.WHITE)
                    }
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
                                                .update(result.toMap())
                                                .addOnSuccessListener { Log.d(tag, "Setting data succeeded.") }
                                                .addOnFailureListener { e -> Log.e(tag, "Setting data failed with: $e") }
                                        findViewById<TextView>(R.id.currencyAmount_bank).text = currency.toString()
                                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.LTGRAY)
                                        findViewById<TextView>(R.id.goldAmount_bank).setTextColor(Color.RED)
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
                                                .update(result.toMap())
                                                .addOnSuccessListener { Log.d(tag, "Setting data succeeded.") }
                                                .addOnFailureListener { e -> Log.e(tag, "Setting data failed with: $e") }
                                        findViewById<TextView>(R.id.currencyAmount_bank).text = currency.toString()
                                        findViewById<TextView>(R.id.goldAmount_bank).text = gold.toString()
                                        findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.RED)
                                        findViewById<TextView>(R.id.goldAmount_bank).setTextColor(Color.LTGRAY)
                                    }
                                }
                            }
                        }
                        findViewById<EditText>(R.id.amount_bank).text.clear()
                    }
                    when(lowerCase) {
                        "quid" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.YELLOW)
                        "shil" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.CYAN)
                        "dolr" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.GREEN)
                        "peny" -> findViewById<TextView>(R.id.currencyAmount_bank).setTextColor(Color.MAGENTA)
                    }
                }
                .addOnFailureListener { e -> Log.e(tag, "Reading data failed with: $e") }
    }

}
