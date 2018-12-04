package com.uoe.zhanshenlc.coinz

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.Image
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
import java.text.SimpleDateFormat
import java.util.*

class FriendListActivity : AppCompatActivity() {

    private val tag = "FriendListActivity"
    private val fireStore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        val listView: ListView = findViewById(R.id.listView_friendList)
        fireStore.collection("friends").document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    Log.d(tag, "User friends data found.")
                    val newRequest = it.data!!["newRequest"] as Boolean
                    val friendList = it.data!!["friendList"] as ArrayList<String>
                    val friendWaitConfirm = it.data!!["friendWaitConfirm"] as ArrayList<String>

                    fireStore.collection("today coins list")
                            .document(mAuth.currentUser?.email.toString()).get()
                            .addOnSuccessListener {
                                val myCurrecies = it.data!!["currencies"] as HashMap<String, String>
                                val myValues = it.data!!["values"] as HashMap<String, Double>
                                listView.adapter = FriendListActivity.MyCustomAdapter(this, fireStore, mAuth,
                                        newRequest, friendList, friendWaitConfirm, myCurrecies, myValues, today)
                            }

                }

        val toolbar: Toolbar = findViewById(R.id.toolbar_friendList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.addFriend_friendList).setOnClickListener {
            startActivity(Intent(applicationContext, AddFriendActivity::class.java))
        }
    }

    private class MyCustomAdapter(context: Context, fireStore: FirebaseFirestore, auth: FirebaseAuth,
                                  newRequest: Boolean, friendList: ArrayList<String>,
                                  friendWaitConfirm: ArrayList<String>, myCurrencies: HashMap<String, String>,
                                  myValues: HashMap<String, Double>, today: String): BaseAdapter() {

        private val mContext = context
        private val mFirestore = fireStore
        private val mAuth = auth
        private val b = newRequest
        private val listWait = friendWaitConfirm
        private val myCurr = myCurrencies
        private val myVal = myValues
        private val date = today

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

            val friendEmail = list[position]
            view.findViewById<TextView>(R.id.email_friendList).text = friendEmail
            mFirestore.collection("users").document(friendEmail).get()
                    .addOnSuccessListener {
                        Log.d(tag, "Data found")
                        val name = it.data!!["name"] as String
                        view.findViewById<TextView>(R.id.name_friendList).text = name
                    }
            view.findViewById<ImageButton>(R.id.unFriend_friendList).setOnClickListener {
                mFirestore.collection("friends").document(friendEmail).get()
                        .addOnSuccessListener {
                            Log.d(tag, "Data found")
                            val newRequest = it.data!!["newRequest"] as Boolean
                            val friendList = it.data!!["friendList"] as ArrayList<String>
                            val friendWaitConfirm = it.data!!["friendWaitConfirm"] as ArrayList<String>
                            friendList.remove(mAuth.currentUser?.email.toString())
                            mFirestore.collection("friends").document(friendEmail)
                                    .set(FriendLists(newRequest, friendList, friendWaitConfirm).toMap())
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                        }
                list.remove(friendEmail)
                mFirestore.collection("friends").document(mAuth.currentUser?.email.toString())
                        .set(FriendLists(b, list, listWait).toMap())
                        .addOnSuccessListener {  }
                        .addOnFailureListener {  }
                notifyDataSetChanged()
            }
            val toFriendBtn = view.findViewById<ImageButton>(R.id.toFriend_friendList)
            toFriendBtn.setOnClickListener {
                val popupMenu = PopupMenu(mContext, toFriendBtn)
                for (id in myCurr.keys) {
                    when(myCurr[id]) {
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
            return view
        }

        private fun sendCoin(id: String, friendEmail: String) {
            mFirestore.collection("today coins list").document(friendEmail).get()
                    .addOnSuccessListener {
                        val friendDate = it.data!!["date"] as String
                        val friendCurr: HashMap<String, String>
                        val friendVal: HashMap<String, Double>
                        if (friendDate != date) {
                            friendCurr = HashMap()
                            friendVal = HashMap()
                        } else {
                            friendCurr = it.data!!["currencies"] as HashMap<String, String>
                            friendVal = it.data!!["values"] as HashMap<String, Double>
                        }
                        if (friendCurr.containsKey(id)) {
                            Toast.makeText(mContext, "Your friend has this coin", Toast.LENGTH_SHORT).show()
                        } else {
                            friendCurr[id] = myCurr[id]!!
                            friendVal[id] = myVal[id]!!
                            mFirestore.collection("today coins list").document(friendEmail)
                                    .update(CoinToday(friendCurr, friendVal).update())
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                            mFirestore.collection("today coins list")
                                    .document(mAuth.currentUser?.email.toString())
                                    .update(CoinToday(myCurr, myVal).update())
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener {  }
                        }
                    }
        }

    }

}
