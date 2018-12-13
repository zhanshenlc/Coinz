package com.uoe.zhanshenlc.coinz

import android.content.Context
import android.content.Intent
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
import com.uoe.zhanshenlc.coinz.dataModels.FriendLists
import com.uoe.zhanshenlc.coinz.dataModels.Modes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FriendListActivity : AppCompatActivity() {

    private val tag = "FriendListActivity"
    private val fireStore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        // Get friend list data and today coin lists data
        val listView = findViewById<ListView>(R.id.listView_friendList)
        fireStore.collection("friends").document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    Log.d(tag, "User friends data found.")
                    val friendList = it.data!!["friendList"] as ArrayList<String>
                    fireStore.collection("today coins list")
                            .document(mAuth.currentUser?.email.toString()).get()
                            .addOnSuccessListener {
                                val myCurrecies = it.data!!["currencies"] as HashMap<String, String>
                                val myValues = it.data!!["values"] as HashMap<String, Double>
                                val inBankCoinIDToday = it.data!!["inBankCoinIDToday"] as ArrayList<String>
                                val purchasedCoinIDToday = it.data!!["purchasedCoinIDToday"] as ArrayList<String>
                                val sentCoinIDToday = it.data!!["sentCoinIDToday"] as ArrayList<String>
                                listView.adapter = FriendListActivity.MyCustomAdapter(this, fireStore, mAuth,
                                        friendList, myCurrecies, myValues, today, inBankCoinIDToday,
                                        purchasedCoinIDToday, sentCoinIDToday)

                            }

                }

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_friendList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.receivedCoins_friendList).setOnClickListener {
            startActivity(Intent(applicationContext, BankInReceiveActivity::class.java))
        }
    }

    // List view to show friends and a popup menu for showing coins that could be sent
    private class MyCustomAdapter(context: Context, fireStore: FirebaseFirestore, auth: FirebaseAuth,
                                  friendList: ArrayList<String>, myCurrencies: HashMap<String, String>,
                                  myValues: HashMap<String, Double>, today: String,
                                  inBankCoinIDToday: ArrayList<String>,
                                  purchasedCoinIDToday: ArrayList<String>,
                                  sentCoinIDToday: ArrayList<String>): BaseAdapter() {

        private val mContext = context
        private val mFirestore = fireStore
        private val mAuth = auth
        private val myCurr = myCurrencies
        private val myVal = myValues
        private val date = today
        private val inBanks = inBankCoinIDToday
        private val purchases = purchasedCoinIDToday
        private val sents = sentCoinIDToday

        private var list = friendList
        private val tag = "FriendListActivity"

        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val view = layoutInflater.inflate(R.layout.friend_list_item, parent, false)

            // Show friend email and name
            val friendEmail = list[position]
            view.findViewById<TextView>(R.id.email_friendList).text = friendEmail
            mFirestore.collection("users").document(friendEmail).get()
                    .addOnSuccessListener {
                        Log.d(tag, "Data found")
                        val name = it.data!!["name"] as String
                        view.findViewById<TextView>(R.id.name_friendList).text = name
                    }
            // Remove a friend
            view.findViewById<ImageButton>(R.id.unFriend_friendList).setOnClickListener {
                // Retrieve friend's friend list, remove user from that list and update to FireStore
                mFirestore.collection("friends").document(friendEmail).get()
                        .addOnSuccessListener {
                            Log.d(tag, "Data found")
                            val friendList = it.data!!["friendList"] as ArrayList<String>
                            friendList.remove(mAuth.currentUser?.email.toString())
                            mFirestore.collection("friends").document(friendEmail)
                                    .update(FriendLists(friendList).updateFriendList())
                                    .addOnSuccessListener { Log.d(tag, "Read data success") }
                                    .addOnFailureListener { Log.e(tag, "Fail to set data with: $it") }
                        }
                // Remove friend from friend list and update to FireStore
                list.remove(friendEmail)
                mFirestore.collection("friends").document(mAuth.currentUser?.email.toString())
                        .update(FriendLists(list).updateFriendList())
                        .addOnSuccessListener { Log.d(tag, "Read data success") }
                        .addOnFailureListener { Log.e(tag, "Fail to set data with: $it") }
                notifyDataSetChanged()
            }
            // Send coins to friend
            val toFriendBtn = view.findViewById<ImageButton>(R.id.toFriend_friendList)
            toFriendBtn.setOnClickListener {
                // Check if user has already saved 25 coins in the bank account or not
                if (inBanks.size < 1) {
                    Toast.makeText(mContext, "You can only send your spare coins to your friends. " +
                            "Please save 25 coins in your account first.", Toast.LENGTH_LONG).show()
                } else {
                    // Popup menu for coins
                    val popupMenu = PopupMenu(mContext, toFriendBtn)
                    for (id in myCurr.keys) {
                        // Coins collected can only be used for one purpose once
                        if (inBanks.contains(id) || purchases.contains(id) || sents.contains(id)) {
                            continue
                        }
                        when (myCurr[id]) {
                            "QUID" ->
                                popupMenu.menu.add(myVal[id].toString()).setIcon(R.drawable.ic_quid_24dp).setOnMenuItemClickListener {
                                    sendCoin(id, friendEmail)
                                    false
                                }
                            "SHIL" ->
                                popupMenu.menu.add(myVal[id].toString()).setIcon(R.drawable.ic_shil_24dp).setOnMenuItemClickListener {
                                    sendCoin(id, friendEmail)
                                    false
                                }
                            "DOLR" ->
                                popupMenu.menu.add(myVal[id].toString()).setIcon(R.drawable.ic_dolr_24dp).setOnMenuItemClickListener {
                                    sendCoin(id, friendEmail)
                                    false
                                }
                            "PENY" ->
                                popupMenu.menu.add(myVal[id].toString()).setIcon(R.drawable.ic_peny_24dp).setOnMenuItemClickListener {
                                    sendCoin(id, friendEmail)
                                    false
                                }
                        }
                    }

                    // Add image view on popup menu
                    // https://www.youtube.com/watch?v=ncHjCsoj0Ws
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
            }
            return view
        }

        // sendCoin function for different coins and friends
        private fun sendCoin(id: String, friendEmail: String) {
            mFirestore.collection("today coins list").document(friendEmail).get()
                    .addOnSuccessListener {
                        // Check whether friend's today coin lists are up to date or not
                        val friendDate = it.data!!["date"] as String
                        val receivedCoinCurrenciesToday: HashMap<String, String>
                        val receivedCoinValuesToday: HashMap<String, Double>
                        val receivedCoinFromToday: HashMap<String, String>
                        if (friendDate != date) {
                            mFirestore.collection("today coins list").document(friendEmail)
                                    .set(CoinToday(date).toMap())
                                    .addOnSuccessListener { Log.d(tag, "New data set for friend") }
                                    .addOnFailureListener { Log.e(tag, "Fail to set new data for friend with: $it") }
                            receivedCoinCurrenciesToday = HashMap()
                            receivedCoinValuesToday = HashMap()
                            receivedCoinFromToday = HashMap()
                        } else {
                            receivedCoinCurrenciesToday = it.data!!["receivedCoinCurrenciesToday"] as HashMap<String, String>
                            receivedCoinValuesToday = it.data!!["receivedCoinValuesToday"] as HashMap<String, Double>
                            receivedCoinFromToday = it.data!!["receivedCoinFromToday"] as HashMap<String, String>
                        }
                        // Cannot send coins your friend have already received by someone else
                        if (receivedCoinCurrenciesToday.containsKey(id)) {
                            Toast.makeText(mContext, "Someone else have sent this coin to your friend.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Update received lists for friend to FireStore
                            receivedCoinCurrenciesToday[id] = myCurr[id]!!
                            receivedCoinValuesToday[id] = myVal[id]!!
                            receivedCoinFromToday[id] = mAuth.currentUser?.email.toString()
                            mFirestore.collection("today coins list").document(friendEmail)
                                    .update(CoinToday(receivedCoinCurrenciesToday, receivedCoinValuesToday,
                                            receivedCoinFromToday).updateReceive())
                                    .addOnSuccessListener { Log.d(tag, "Update data success") }
                                    .addOnFailureListener { Log.e(tag, "Fail to update data with: $it") }
                            sents.add(id)
                            mFirestore.collection("today coins list")
                                    .document(mAuth.currentUser?.email.toString())
                                    .update(CoinToday(sents, Modes.SEND).updateSend())
                                    .addOnSuccessListener { Log.d(tag, "Update data success") }
                                    .addOnFailureListener { Log.e(tag, "Fail to update data with: $it") }
                        }
                    }
        }

    }

}