package com.uoe.zhanshenlc.coinz

import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.BankAccount
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TodayListActivity : AppCompatActivity() {

    private val tag = "TodayListActivity"
    private var mAuth = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_list)

        // https://www.youtube.com/watch?v=P2I8PGLZEVc
        val listView = findViewById<ListView>(R.id.listView_coinTodayList)
        fireStore.collection("users").document(mAuth.uid.toString())
                .collection("coins").document("today")
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.d(tag, "Errors reading today's coin data: $firebaseFirestoreException")
                    } else {
                        Log.d(tag, "Successfully read today's coin data")
                        val currencies = documentSnapshot!!.data!!["currencies"] as HashMap<String, String>
                        val values = documentSnapshot.data!!["values"] as HashMap<String, Double>
                        // Check date of bank account
                        fireStore.collection("users").document(mAuth.uid.toString())
                                .collection("coins").document("bankAccount")
                                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                                    if (firebaseFirestoreException != null) {
                                        Log.d(tag, "Errors reading bank account data: $firebaseFirestoreException")
                                    } else {
                                        Log.d(tag, "Successfully read bank account data")
                                        if (documentSnapshot!!.data!!["todayDate"] != today) {
                                            Log.d(tag, "New bank account data for today")
                                            val quid = documentSnapshot.data!!["quid"] as Double
                                            val shil = documentSnapshot.data!!["shil"] as Double
                                            val dolr = documentSnapshot.data!!["dolr"] as Double
                                            val peny = documentSnapshot.data!!["peny"] as Double
                                            val gold = documentSnapshot.data!!["gold"] as Double
                                            fireStore.collection("users").document(mAuth.uid.toString())
                                                    .collection("coins").document("bankAccount")
                                                    .set(BankAccount(today, ArrayList(), ArrayList(), ArrayList(),
                                                            quid, shil, dolr, peny, gold).toMap())
                                            listView.adapter = MyCustomAdapter(this, currencies, values, fireStore, mAuth,
                                                    ArrayList(), ArrayList(), ArrayList())
                                        } else {
                                            Log.d(tag, "Re-visit bank account data today")
                                            val inBankCoinIDToday = documentSnapshot.data!!["inBankCoinIDToday"] as ArrayList<String>
                                            val purchasedCoinIDToday = documentSnapshot.data!!["purchasedCoinIDToday"] as ArrayList<String>
                                            val sentCoinIDToday = documentSnapshot.data!!["sentCoinIDToday"] as ArrayList<String>
                                            listView.adapter = MyCustomAdapter(this, currencies, values, fireStore, mAuth,
                                                    inBankCoinIDToday, purchasedCoinIDToday, sentCoinIDToday)
                                        }
                                    }
                                }

                    }
                }

        val toolbar: Toolbar = findViewById(R.id.toolbar_coinTodayList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private class MyCustomAdapter(context: Context, currencies: HashMap<String, String>,
                                  values: HashMap<String, Double>, fireStore: FirebaseFirestore,
                                  auth: FirebaseAuth, inBankCoinIDToday: ArrayList<String>,
                                  purchasedCoinIDToday: ArrayList<String>,
                                  sentCoinIDToday: ArrayList<String>): BaseAdapter() {

        private val mContext: Context = context
        private val mCurrencies: HashMap<String, String> = currencies
        private val mValues: HashMap<String, Double> = values
        private val mFireStore = fireStore
        private val mAuth = auth
        private var inBank = inBankCoinIDToday
        private var purchased = purchasedCoinIDToday
        private var sent = sentCoinIDToday

        private val coinIDs = checkID(currencies.keys.toList(), inBank, purchased, sent)
        private val tag = "TodayListActivity"

        fun checkID(cIDs: List<String>, banked: ArrayList<String>, bought: ArrayList<String>,
                    given: ArrayList<String>): List<String> {
            val result = ArrayList(cIDs)
            for (c in cIDs) {
                if (banked.contains(c) || bought.contains(c) || given.contains(c)) { result.remove(c) }
            }
            return result.toList()
        }

        override fun getCount(): Int {
            return coinIDs.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return position
        }

        private fun getCoinID(position: Int): String {
            return coinIDs[position]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val view = layoutInflater.inflate(R.layout.today_list_item, parent, false)

            val cID = getCoinID(position)
            view.findViewById<TextView>(R.id.coinID_todayListView).text = cID
            view.findViewById<TextView>(R.id.coinValue_todayListView).text = mValues[cID].toString()
            val currency = mCurrencies[cID]
            view.findViewById<TextView>(R.id.coinCurrency_todayListView).text = currency
            when(currency) {
                // "SHIL" "#0000ff" "QUID" "#ffdf00" "DOLR" "#008000" "PENY" "#ff0000"
                "PENY" -> view.setBackgroundColor(Color.parseColor("#ff0000"))
                "SHIL" -> view.setBackgroundColor(Color.parseColor("#0000ff"))
                "QUID" -> view.setBackgroundColor(Color.parseColor("#ffdf00"))
                "DOLR" -> view.setBackgroundColor(Color.parseColor("#008000"))
            }
            view.findViewById<ImageButton>(R.id.inBankBtn_todayListView).setOnClickListener {
                mFireStore.collection("users").document(mAuth.uid.toString())
                        .collection("coins").document("bankAccount")
                        .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                            if (firebaseFirestoreException != null) {
                                Log.d(tag, "Errors reading bank account data: $firebaseFirestoreException")
                            } else {
                                Log.d(tag, "Successfully read bank account data")
                                if (inBank.contains(cID)) {
                                    Log.d(tag, "Already banked in")
                                    Toast.makeText(mContext, "Already banked in", Toast.LENGTH_SHORT).show()
                                }
                                else if (purchased.contains(cID)) {
                                    Log.d(tag, "Already spent")
                                    Toast.makeText(mContext, "Already spent", Toast.LENGTH_SHORT).show()
                                }
                                else if (sent.contains(cID)) {
                                    Log.d(tag, "Already sent")
                                    Toast.makeText(mContext, "Already sentd", Toast.LENGTH_SHORT).show()
                                } else if (inBank.size == 3) {
                                    Log.d(tag, "Today's banking limit reached")
                                    Toast.makeText(mContext, "Today's banking limit reached", Toast.LENGTH_SHORT).show()
                                } else {
                                    var quid = documentSnapshot!!.data!!["quid"] as Double
                                    var shil = documentSnapshot.data!!["shil"] as Double
                                    var dolr = documentSnapshot.data!!["dolr"] as Double
                                    var peny = documentSnapshot.data!!["peny"] as Double
                                    val gold = documentSnapshot.data!!["gold"] as Double
                                    val todayDate = documentSnapshot.data!!["todayDate"] as String
                                    when(currency) {
                                        "PENY" -> peny += mValues[cID]!!
                                        "SHIL" -> shil += mValues[cID]!!
                                        "QUID" -> quid += mValues[cID]!!
                                        "DOLR" -> dolr += mValues[cID]!!
                                    }
                                    inBank.add(cID)
                                    mFireStore.collection("users").document(mAuth.uid.toString())
                                            .collection("coins").document("bankAccount")
                                            .set(BankAccount(todayDate, inBank, purchased, sent, quid, shil, dolr, peny, gold).toMap())
                                            .addOnSuccessListener {
                                                Log.d(tag, "Updated")
                                                Toast.makeText(mContext, "Banked", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.d(tag, "Failed with: $e")
                                                Toast.makeText(mContext, "Failed with: $e", Toast.LENGTH_SHORT).show()
                                            }

                                }
                            }
                        }
            }
            view.findViewById<ImageButton>(R.id.shopBtn_todayListView).setOnClickListener {
                Toast.makeText(mContext, "shop", Toast.LENGTH_SHORT).show()
            }
            view.findViewById<ImageButton>(R.id.toFriendBtn_todayListdView).setOnClickListener {
                Toast.makeText(mContext, "friend", Toast.LENGTH_SHORT).show()
            }
            return view
        }
    }
}
