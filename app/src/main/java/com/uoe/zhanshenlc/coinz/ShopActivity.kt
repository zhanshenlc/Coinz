package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.CoinToday
import com.uoe.zhanshenlc.coinz.dataModels.Goods
import com.uoe.zhanshenlc.coinz.dataModels.Modes
import com.uoe.zhanshenlc.coinz.dataModels.ShoppingBag
import java.text.SimpleDateFormat
import java.util.*

class ShopActivity : AppCompatActivity() {

    private val tag = "ShopActivity"
    private val fireStore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        val toolbar: Toolbar = findViewById(R.id.toolbar_shop)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        fireStore.collection("shopping carts")
                .document(mAuth.currentUser?.email.toString())
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e(tag, "Error: $firebaseFirestoreException")
                    } else {
                        var meat: Long = 0
                        var bread: Long = 0
                        var water: Long = 0
                        var pill: Long = 0
                        if (documentSnapshot!!.data == null || documentSnapshot.data!!["date"] != today) {
                            fireStore.collection("shopping carts")
                                    .document(mAuth.currentUser?.email.toString())
                                    .set(ShoppingBag(today).toMap())
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                        } else {
                            meat = documentSnapshot.data!!["meat"] as Long
                            bread = documentSnapshot.data!!["bread"] as Long
                            water = documentSnapshot.data!!["water"] as Long
                            pill = documentSnapshot.data!!["pill"] as Long
                        }
                        if (meat.toInt() * bread.toInt() * water.toInt() == 1 || pill.toInt() == 3) {
                            findViewById<CheckBox>(R.id.missionStatus_shop).isChecked = true
                            findViewById<ImageButton>(R.id.meatBtn_shop).isClickable = false
                            findViewById<ImageButton>(R.id.breadBtn_shop).isClickable = false
                            findViewById<ImageButton>(R.id.waterBtn_shop).isClickable = false
                            findViewById<ImageButton>(R.id.pillBtn_shop).isClickable = false
                        } else {
                            if (meat.toInt() == 1)
                                    findViewById<ImageButton>(R.id.meatBtn_shop).isClickable = false
                            if (bread.toInt() == 1)
                                    findViewById<ImageButton>(R.id.breadBtn_shop).isClickable = false
                            if (water.toInt() == 1)
                                    findViewById<ImageButton>(R.id.waterBtn_shop).isClickable = false
                        }
                        findViewById<TextView>(R.id.meatAmount_shop).text = meat.toString() //"$meat / 1"
                        findViewById<TextView>(R.id.breadAmount_shop).text = bread.toString() //"$bread / 1"
                        findViewById<TextView>(R.id.waterAmount_shop).text = water.toString() //"$water / 1"
                        findViewById<TextView>(R.id.pillAmount_shop).text = pill.toString() //"$pill / 3"
                    }
                }

        findViewById<ImageButton>(R.id.meatBtn_shop).setOnClickListener {
            purchase("SHIL", R.id.meatBtn_shop, R.drawable.ic_shil_24dp, Goods.MEAT, R.id.meatAmount_shop)
        }
        findViewById<ImageButton>(R.id.breadBtn_shop).setOnClickListener {
            purchase("DOLR", R.id.breadBtn_shop, R.drawable.ic_dolr_24dp, Goods.BREAD, R.id.breadAmount_shop)
        }
        findViewById<ImageButton>(R.id.waterBtn_shop).setOnClickListener {
            purchase("PENY", R.id.waterBtn_shop, R.drawable.ic_quid_24dp, Goods.WATER, R.id.waterAmount_shop)
        }
        findViewById<ImageButton>(R.id.pillBtn_shop).setOnClickListener {
            purchase("QUID", R.id.pillBtn_shop, R.drawable.ic_quid_24dp, Goods.PILL, R.id.quidAmount_wallet)
        }
    }

    private fun purchase(currency: String, button: Int, drawable: Int,
                         goodType: Goods, textView: Int) {
        fireStore.collection("today coins list")
                .document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    val myCurrecies = it.data!!["currencies"] as HashMap<String, String>
                    val myValues = it.data!!["values"] as HashMap<String, Double>
                    val inBankCoinIDToday = it.data!!["inBankCoinIDToday"] as ArrayList<String>
                    val purchasedCoinIDToday = it.data!!["purchasedCoinIDToday"] as ArrayList<String>
                    val sentCoinIDToday = it.data!!["sentCoinIDToday"] as ArrayList<String>
                    for (id in myCurrecies.keys) {
                        if (myCurrecies[id] != currency) { continue }
                        if (inBankCoinIDToday.contains(id) || purchasedCoinIDToday.contains(id)
                                || sentCoinIDToday.contains(id)) { continue }
                        val popupMenu = PopupMenu(this, findViewById(button))
                        popupMenu.menu.add(myValues[id].toString()).setIcon(drawable).setOnMenuItemClickListener {
                            purchasedCoinIDToday.add(id)
                            val currentAmount = findViewById<TextView>(textView).text.toString()
                            fireStore.collection("today coins list")
                                    .document(mAuth.currentUser?.email.toString())
                                    .update(CoinToday(purchasedCoinIDToday, Modes.BUY).updatePurchased())
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                            fireStore.collection("shopping carts")
                                    .document(mAuth.currentUser?.email.toString())
                                    .update(ShoppingBag((currentAmount.toInt() + 1).toLong(), goodType).update(goodType))
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                            findViewById<TextView>(textView).text =
                                    (findViewById<TextView>(textView).text.toString().toInt() + 1).toString()

                            val meat = findViewById<TextView>(R.id.meatAmount_shop).text.toString().toInt()
                            val bread = findViewById<TextView>(R.id.breadAmount_shop).text.toString().toInt()
                            val water = findViewById<TextView>(R.id.waterAmount_shop).text.toString().toInt()
                            val pill = findViewById<TextView>(R.id.pillAmount_shop).text.toString().toInt()
                            if (meat * bread * water == 1 || pill == 3) {
                                findViewById<CheckBox>(R.id.missionStatus_shop).isChecked = true
                                findViewById<ImageButton>(R.id.meatBtn_shop).isClickable = false
                                findViewById<ImageButton>(R.id.breadBtn_shop).isClickable = false
                                findViewById<ImageButton>(R.id.waterBtn_shop).isClickable = false
                                findViewById<ImageButton>(R.id.pillBtn_shop).isClickable = false
                            } else {
                                if (meat == 1)
                                    findViewById<ImageButton>(R.id.meatBtn_shop).isClickable = false
                                if (bread == 1)
                                    findViewById<ImageButton>(R.id.breadBtn_shop).isClickable = false
                                if (water== 1)
                                    findViewById<ImageButton>(R.id.waterBtn_shop).isClickable = false
                            }
                            false
                        }
                    }
                }
                .addOnFailureListener {  }
    }

}