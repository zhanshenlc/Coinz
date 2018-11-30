package com.uoe.zhanshenlc.coinz

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TodayListActivity : AppCompatActivity() {

    private val tag = "TodayListActivity"
    private var mAuth = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_list)

        val listView = findViewById<ListView>(R.id.listView_coinTodayList)
        val a = fireStore.collection("users").document(mAuth.uid.toString())
                .collection("coins").document("today")
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.d(tag, "Errors reading today's coin data: $firebaseFirestoreException")
                    } else {
                        Log.d(tag, "Successfully read today's coin data")
                        val currencies = documentSnapshot!!.data!!["currencies"] as HashMap<String, String>
                        val values = documentSnapshot.data!!["values"] as HashMap<String, Double>
                        listView.adapter = MyCustomAdapter(this, currencies, values)
                    }
                }
    }

    private class MyCustomAdapter(context: Context, currencies: HashMap<String, String>,
                                  values: HashMap<String, Double>): BaseAdapter() {

        private val mContext: Context = context
        private val mCurrencies: HashMap<String, String> = currencies
        private val mValues: HashMap<String, Double> = values
        private val coinIDs = currencies.keys.toList()

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
            val view = layoutInflater.inflate(R.layout.today_listview_item, parent, false)

            view.findViewById<TextView>(R.id.coinID_todayListView).text = getCoinID(position)
            view.findViewById<TextView>(R.id.coinValue_todayListView).text = mValues[getCoinID(position)].toString()
            val currency = mCurrencies[getCoinID(position)]
            view.findViewById<TextView>(R.id.coinCurrency_todayListView).text = currency
            val imageButton = view.findViewById<ImageButton>(R.id.inBankBtn_todayListView)
            when(currency) {
                "PENY" -> {
                    imageButton.setImageResource(R.drawable.green_coin_24)
                    view.setBackgroundColor(Color.RED)
                }
            }


            view.findViewById<View>(R.id.inBankBtn_todayListView).setOnClickListener {
                Toast.makeText(mContext, "???", Toast.LENGTH_SHORT).show()
            }
            return view
        }
    }
}
