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
import com.uoe.zhanshenlc.coinz.dataModels.FriendLists

class FriendListActivity : AppCompatActivity() {

    private val tag = "FriendListActivity"
    private val fireStore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

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
                    listView.adapter = FriendListActivity.MyCustomAdapter(this, fireStore, mAuth,
                            newRequest, friendList, friendWaitConfirm)
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
                                  friendWaitConfirm: ArrayList<String>): BaseAdapter() {

        private val mContext = context
        private val mFirestore = fireStore
        private val mAuth = auth
        private val b = newRequest
        private val listWait = friendWaitConfirm

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
            return view
        }
    }

}
