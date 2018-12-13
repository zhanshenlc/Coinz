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
import com.uoe.zhanshenlc.coinz.dataModels.CoinToday
import com.uoe.zhanshenlc.coinz.dataModels.Modes

class BankInReceiveActivity : AppCompatActivity() {

    private val tag = "BankInReceiveActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_in_receive)

        val listView = findViewById<ListView>(R.id.listView_bankInReceive)
        fireStore.collection("today coins list")
                .document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    val receivedCoinCurrenciesToday = it.data!!["receivedCoinCurrenciesToday"] as HashMap<String, String>
                    val receivedCoinValuesToday = it.data!!["receivedCoinValuesToday"] as HashMap<String, Double>
                    val receivedCoinFromToday = it.data!!["receivedCoinFromToday"] as HashMap<String, String>
                    val receivedBankedCoinIDToday = it.data!!["receivedBankedCoinIDToday"] as ArrayList<String>
                    val inBankCoinIDToday = it.data!!["inBankCoinIDToday"] as ArrayList<String>
                    listView.adapter = BankInReceiveActivity.MyCustomAdapter(this, mAuth, fireStore,
                            receivedCoinCurrenciesToday, receivedCoinValuesToday, receivedCoinFromToday,
                            receivedBankedCoinIDToday, inBankCoinIDToday)
                }

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_bankInReceive)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private class MyCustomAdapter(context: Context, auth: FirebaseAuth, fireStore: FirebaseFirestore,
                                  receivedCoinCurrenciesToday: HashMap<String, String>,
                                  receivedCoinValuesToday: HashMap<String, Double>,
                                  receivedCoinFromToday: HashMap<String, String>,
                                  receivedBankedCoinIDToday: ArrayList<String>,
                                  inBankCoinIDToday: ArrayList<String>): BaseAdapter() {

        private val tag = "BankInReceiveActivity"
        private val mContext = context
        private val mFireStore = fireStore
        private val mAuth = auth
        private val currencies = receivedCoinCurrenciesToday
        private val values = receivedCoinValuesToday
        private val emails = receivedCoinFromToday
        private val bankReceive = receivedBankedCoinIDToday
        private val bankCollect = inBankCoinIDToday

        override fun getCount(): Int {
            return currencies.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val view: View = when (convertView) {
                null -> layoutInflater.inflate(R.layout.bank_in_receive_list_item, parent, false)
                else -> convertView
            }

            val coinID = currencies.keys.toList()[position]
            val curr = currencies[coinID]
            val value = values[coinID]
            val email = emails[coinID]
            view.findViewById<TextView>(R.id.coinID_BankInReceiveListView).text = coinID
            view.findViewById<TextView>(R.id.coinCurrency_BankInReceiveListView).text = curr
            view.findViewById<TextView>(R.id.coinValue_BankInReceiveListView).text = value.toString()
            view.findViewById<TextView>(R.id.email_BankInReceiveListView).text = email
            when (curr) {
            // "SHIL" "#0000ff" "QUID" "#ffdf00" "DOLR" "#008000" "PENY" "#ff0000"
                "PENY" -> view.setBackgroundColor(Color.parseColor("#967d7f"))
                "SHIL" -> view.setBackgroundColor(Color.parseColor("#90b7c9"))
                "QUID" -> view.setBackgroundColor(Color.parseColor("#d6bea9"))
                "DOLR" -> view.setBackgroundColor(Color.parseColor("#909f88"))
            }
            view.findViewById<ImageButton>(R.id.inBankBtn_BankInReceiveListView).setOnClickListener {
                when {
                    bankReceive.contains(coinID) -> {
                        Toast.makeText(mContext, "Cannot bank in the same coin twice", Toast.LENGTH_SHORT).show()
                        view.setBackgroundColor(Color.GRAY)
                    }
                    bankCollect.contains(coinID) -> {
                        Toast.makeText(mContext, "A same coin has been collected and banked in", Toast.LENGTH_SHORT).show()
                        view.setBackgroundColor(Color.GRAY)
                    }
                    else -> {
                        mFireStore.collection("bank accounts").document(mAuth.uid.toString()).get()
                                .addOnSuccessListener {
                                    val currLowerCase = curr?.toLowerCase()
                                    var amount = it.data!![currLowerCase] as Double
                                    if (value != null) { amount += value }
                                    if (currLowerCase != null) {
                                        val result = HashMap<String, Double>()
                                        result[currLowerCase] = amount
                                        mFireStore.collection("bank accounts").document(mAuth.uid.toString())
                                                .update(result.toMap())
                                                .addOnSuccessListener { Log.d(tag, "Update data successfully") }
                                                .addOnFailureListener { Log.e(tag, "Fail to update with: $it") }
                                    }
                                }
                                .addOnFailureListener { Log.e(tag, "Fail to get data with: $it") }
                        bankReceive.add(coinID)
                        mFireStore.collection("today coins list")
                                .document(mAuth.currentUser?.email.toString())
                                .update(CoinToday(bankReceive, Modes.RECEIVE_BANKED).updateReceiveBanked())
                                .addOnSuccessListener {
                                    Log.d(tag, "Update data successfully")
                                    view.setBackgroundColor(Color.GRAY)
                                }
                                .addOnFailureListener { Log.e(tag, "Fail to update with: $it") }
                    }
                }
            }
            return view
        }

    }

}
