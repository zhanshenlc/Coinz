package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.*
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

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_shop)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Check user progress
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
                            // New visit today, so new data set
                            fireStore.collection("shopping carts")
                                    .document(mAuth.currentUser?.email.toString())
                                    .set(ShoppingBag(today).toMap())
                                    .addOnSuccessListener { Log.d(tag, "Set data success") }
                                    .addOnFailureListener { Log.e(tag, "Set data fail with: $it") }
                        } else {
                            // Not new visit today, read most recent progress
                            meat = documentSnapshot.data!!["meat"] as Long
                            bread = documentSnapshot.data!!["bread"] as Long
                            water = documentSnapshot.data!!["water"] as Long
                            pill = documentSnapshot.data!!["pill"] as Long
                        }
                        // Check whether mission is completed or not
                        // 1 meat 1 bread 1 water or 3 pills
                        if (meat.toInt() * bread.toInt() * water.toInt() == 1 || pill.toInt() == 3) {
                            // If mission is completed, shop is closed
                            findViewById<CheckBox>(R.id.missionStatus_shop).isChecked = true
                            findViewById<ImageButton>(R.id.meatBtn_shop).isClickable = false
                            findViewById<ImageButton>(R.id.breadBtn_shop).isClickable = false
                            findViewById<ImageButton>(R.id.waterBtn_shop).isClickable = false
                            findViewById<ImageButton>(R.id.pillBtn_shop).isClickable = false
                        } else {
                            // Could not buy things that has reached limit
                            if (meat.toInt() == 1)
                                    findViewById<ImageButton>(R.id.meatBtn_shop).isClickable = false
                            if (bread.toInt() == 1)
                                    findViewById<ImageButton>(R.id.breadBtn_shop).isClickable = false
                            if (water.toInt() == 1)
                                    findViewById<ImageButton>(R.id.waterBtn_shop).isClickable = false
                        }
                        // Update progress to view
                        findViewById<TextView>(R.id.meatAmount_shop).text = meat.toString() //"$meat / 1"
                        findViewById<TextView>(R.id.breadAmount_shop).text = bread.toString() //"$bread / 1"
                        findViewById<TextView>(R.id.waterAmount_shop).text = water.toString() //"$water / 1"
                        findViewById<TextView>(R.id.pillAmount_shop).text = pill.toString() //"$pill / 3"
                    }
                }

        // Different customised popup menu for different goods (currencies)
        findViewById<ImageButton>(R.id.meatBtn_shop).setOnClickListener {
            val popupMenu = PopupMenu(this, findViewById(R.id.meatBtn_shop))
            purchase("SHIL", R.drawable.ic_shil_24dp, Goods.MEAT, R.id.meatAmount_shop, popupMenu)
        }
        findViewById<ImageButton>(R.id.breadBtn_shop).setOnClickListener {
            val popupMenu = PopupMenu(this, findViewById(R.id.breadBtn_shop))
            purchase("DOLR", R.drawable.ic_dolr_24dp, Goods.BREAD, R.id.breadAmount_shop, popupMenu)
        }
        findViewById<ImageButton>(R.id.waterBtn_shop).setOnClickListener {
            val popupMenu = PopupMenu(this, findViewById(R.id.waterBtn_shop))
            purchase("PENY", R.drawable.ic_peny_24dp, Goods.WATER, R.id.waterAmount_shop, popupMenu)
        }
        findViewById<ImageButton>(R.id.pillBtn_shop).setOnClickListener {
            val popupMenu = PopupMenu(this, findViewById(R.id.pillBtn_shop))
            purchase("QUID", R.drawable.ic_quid_24dp, Goods.PILL, R.id.pillAmount_shop, popupMenu)
        }
    }

    private fun purchase(currency: String, drawable: Int, goodType: Goods, textView: Int, popupMenu: PopupMenu) {
        fireStore.collection("today coins list")
                .document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    val myCurrencies = it.data!!["currencies"] as HashMap<String, String>
                    val myValues = it.data!!["values"] as HashMap<String, Double>
                    val inBankCoinIDToday = it.data!!["inBankCoinIDToday"] as ArrayList<String>
                    val purchasedCoinIDToday = it.data!!["purchasedCoinIDToday"] as ArrayList<String>
                    val sentCoinIDToday = it.data!!["sentCoinIDToday"] as ArrayList<String>
                    for (id in myCurrencies.keys) {
                        // Keep coins with the correct corrency
                        if (myCurrencies[id] != currency) { continue }
                        // Keep coins which has not been used (for any purpose)
                        if (inBankCoinIDToday.contains(id) || purchasedCoinIDToday.contains(id)
                                || sentCoinIDToday.contains(id)) { continue }
                        // Click popup menu item to buy
                        popupMenu.menu.add(myValues[id].toString()).setIcon(drawable).setOnMenuItemClickListener {
                            purchasedCoinIDToday.add(id)
                            val currentAmount = findViewById<TextView>(textView).text.toString()
                            // Update today coins list to FireStore
                            fireStore.collection("today coins list")
                                    .document(mAuth.currentUser?.email.toString())
                                    .update(CoinToday(purchasedCoinIDToday, Modes.BUY).updatePurchased())
                                    .addOnSuccessListener { Log.d(tag, "Update success") }
                                    .addOnFailureListener { Log.e(tag, "Update failed with: $it") }
                            // Update shopping carts to FireStore
                            fireStore.collection("shopping carts")
                                    .document(mAuth.currentUser?.email.toString())
                                    .update(ShoppingBag((currentAmount.toInt() + 1).toLong(), goodType).update(goodType))
                                    .addOnSuccessListener { Log.d(tag, "Update success") }
                                    .addOnFailureListener { Log.e(tag, "Update failed with: $it") }
                            // Update progress view
                            findViewById<TextView>(textView).text =
                                    (findViewById<TextView>(textView).text.toString().toInt() + 1).toString()

                            // Check whether mission is completed or not
                            // Also, make corresponding buttons not clickable
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
                                fireStore.collection("shopping carts")
                                        .document(mAuth.currentUser?.email.toString())
                                        .update(ShoppingBag().complete())
                                        .addOnSuccessListener {  }
                                        .addOnFailureListener {  }
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

                    // Images for popup menu
                    try {
                        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                        fieldMPopup.isAccessible = true
                        val mPopup = fieldMPopup.get(popupMenu)
                        mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopup, true)
                    } catch (e: Exception) {
                        Log.e(tag, "Error showing menu icons")
                    } finally {
                        popupMenu.show()
                    }
                }
                .addOnFailureListener { Log.e(tag, "Fail to get data with: $it") }
    }

}
