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

class WalletActivity : AppCompatActivity() {

    private val tag = "WalletActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        val quidView = findViewById<TextView>(R.id.quidAmount_wallet)
        val shilView = findViewById<TextView>(R.id.shilAmount_wallet)
        val dolrView = findViewById<TextView>(R.id.dolrAmount_wallet)
        val penyView = findViewById<TextView>(R.id.penyAmount_wallet)
        val goldView = findViewById<TextView>(R.id.goldAmount_wallet)

        fireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                .addOnSuccessListener {
                    val quid = it.data!!["quid"] as Double
                    val shil = it.data!!["shil"] as Double
                    val dolr = it.data!!["dolr"] as Double
                    val peny = it.data!!["peny"] as Double
                    val gold = it.data!!["gold"] as Double
                    quidView.text = quid.toString()
                    shilView.text = shil.toString()
                    dolrView.text = dolr.toString()
                    penyView.text = peny.toString()
                    goldView.text = gold.toString()
                    Log.d(tag, "Read data success")
                }
                .addOnFailureListener { e -> Log.d(tag, "Fail to read with: $e") }

        val toolbar: Toolbar = findViewById(R.id.toolbar_wallet)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val listView = findViewById<ListView>(R.id.coinList_wallet)
        fireStore.collection("today coins list").document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    val currencies = it!!.data!!["currencies"] as HashMap<String, String>
                    val values = it.data!!["values"] as HashMap<String, Double>
                    val inBankCoinIDToday = it.data!!["inBankCoinIDToday"] as ArrayList<String>
                    val purchasedCoinIDToday = it.data!!["purchasedCoinIDToday"] as ArrayList<String>
                    val sentCoinIDToday = it.data!!["sentCoinIDToday"] as ArrayList<String>
                    listView.adapter = MyCustomAdapter(this, fireStore, mAuth, currencies, values, inBankCoinIDToday,
                            purchasedCoinIDToday, sentCoinIDToday, quidView, shilView, dolrView, penyView)
                }
    }

    private class MyCustomAdapter(context: Context, fireStore: FirebaseFirestore, auth: FirebaseAuth,
                                  currencies: HashMap<String, String>, values: HashMap<String, Double>,
                                  inBankCoinIDToday: ArrayList<String>, purchasedCoinIDToday: ArrayList<String>,
                                  sentCoinIDToday: ArrayList<String>, quid: TextView, shil: TextView,
                                  dolr: TextView, peny: TextView): BaseAdapter() {

        private val mContext = context
        private val mFirestore = fireStore
        private val mAuth = auth

        private val myCurr = currencies
        private val myVal = values
        private val inBanks = inBankCoinIDToday
        private val purchases = purchasedCoinIDToday
        private val sents = sentCoinIDToday

        private val quidView = quid
        private val shilView = shil
        private val dolrView = dolr
        private val penyView = peny

        private val toBeCollectedIDs = getNotCollectedID(currencies)
        private var num = toBeCollectedIDs.size

        private fun getNotCollectedID(currencies: HashMap<String, String>): ArrayList<String> {
            val result = ArrayList<String>()
            for (id in currencies.keys.toList()) {
                if (inBanks.contains(id) || purchases.contains(id) || sents.contains(id)) { continue }
                result.add(id)
            }
            return result
        }

        override fun getCount(): Int {
            return num
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val view = layoutInflater.inflate(R.layout.wallet_list_item, parent, false)

            val coinID = toBeCollectedIDs[position]
            val coinCurr = myCurr[coinID]
            view.findViewById<TextView>(R.id.coinID_walletListView).text = coinID
            view.findViewById<TextView>(R.id.coinCurrency_walletListView).text = coinCurr
            view.findViewById<TextView>(R.id.coinValue_walletListView).text = myVal[coinID].toString()
            var amountView: TextView? = null
            when(coinCurr) {
            // "SHIL" "#0000ff" "QUID" "#ffdf00" "DOLR" "#008000" "PENY" "#ff0000"
                "PENY" -> {
                    view.setBackgroundColor(Color.parseColor("#967d7f"))
                    amountView = penyView
                }
                "SHIL" -> {
                    view.setBackgroundColor(Color.parseColor("#90b7c9"))
                    amountView = shilView
                }
                "QUID" -> {
                    view.setBackgroundColor(Color.parseColor("#d6bea9"))
                    amountView = quidView
                }
                "DOLR" -> {
                    view.setBackgroundColor(Color.parseColor("#909f88"))
                    amountView = dolrView
                }
            }
            view.findViewById<ImageButton>(R.id.inBankBtn_walletListView).setOnClickListener {
                if (inBanks.size == 25) {
                    Toast.makeText(mContext, "Reached bank in limit: 25", Toast.LENGTH_SHORT).show()
                } else {
                    bankIn(coinID, coinCurr!!, amountView)
                }
            }
            return view
        }

        private fun bankIn(coinID: String, coinCurr: String, amountView: TextView?) {
            val str = coinCurr.toLowerCase()
            mFirestore.collection("bank accounts").document(mAuth.uid.toString()).get()
                    .addOnSuccessListener {
                        var balance = it.data!![str] as Double
                        balance += myVal[coinID]!!
                        amountView?.text = balance.toString()
                        val todayUpdate = HashMap<String, Any>()
                        todayUpdate[str] = balance
                        mFirestore.collection("bank accounts").document(mAuth.uid.toString())
                                .update(todayUpdate.toMap())
                                .addOnSuccessListener { }
                                .addOnFailureListener { }
                        val accountUpdate = HashMap<String, Any>()
                        inBanks.add(coinID)
                        num --
                        this.notifyDataSetChanged()
                        accountUpdate["inBankCoinIDToday"] = inBanks
                        mFirestore.collection("today coins list")
                                .document(mAuth.currentUser?.email.toString())
                                .update(accountUpdate.toMap())
                                .addOnSuccessListener { }
                                .addOnFailureListener { }
                    }
        }

    }

}
