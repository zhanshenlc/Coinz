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

class FriendRequestListActivity : AppCompatActivity() {

    private val tag = "FriendRequestListActivity"
    private val fireStore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request_list)

        // Get friend list and friend wait confirm list
        val listView: ListView = findViewById(R.id.listView_friendRequestList)
        fireStore.collection("friends").document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    Log.d(tag, "User friends data found.")
                    val friendList = it.data!!["friendList"] as ArrayList<String>
                    val friendWaitConfirm = it.data!!["friendWaitConfirm"] as ArrayList<String>
                    listView.adapter = MyCustomAdapter(this, fireStore, mAuth, friendList,
                            friendWaitConfirm)
                }

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_friendRequestList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Add friend button
        findViewById<ImageButton>(R.id.addFriend_friendRequestList).setOnClickListener {
            startActivity(Intent(applicationContext, AddFriendActivity::class.java))
        }
    }

    private class MyCustomAdapter(context: Context, fireStore: FirebaseFirestore, auth: FirebaseAuth,
                                  friendList: ArrayList<String>, friendWaitConfirm: ArrayList<String>)
        : BaseAdapter() {

        private val mContext: Context = context
        private val mFireStore = fireStore
        private val mAuth = auth
        private val list = friendList
        private val listWait = friendWaitConfirm

        private val tag = "FriendRequestListActivity"

        override fun getCount(): Int {
            return listWait.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val view = layoutInflater.inflate(R.layout.friend_request_list_item, parent, false)

            val friendEmail = listWait[position]
            // Retrieve the name of the friend-to-be from FireStore and update view
            view.findViewById<TextView>(R.id.email_friendRequestList).text = friendEmail
            mFireStore.collection("users").document(friendEmail).get()
                    .addOnSuccessListener {
                        Log.d(tag, "Get $friendEmail data success")
                        val name = it.data!!["name"] as String
                        view.findViewById<TextView>(R.id.name_friendRequestList).text = name
                    }
                    .addOnFailureListener { e -> Log.d(tag, "Failed to get $friendEmail data with: $e") }
            // Accept: remove from wait confirm list and add to friend lists of both people
            view.findViewById<ImageButton>(R.id.accept_friendRequestList).setOnClickListener {
                // Retrieve friend list data of friend, add user to list and update to FireStore
                mFireStore.collection("friends").document(friendEmail).get()
                        .addOnSuccessListener {
                            Log.d(tag, "Get $friendEmail friends data success")
                            val friendList = it.data!!["friendList"] as ArrayList<String>
                            friendList.add(mAuth.currentUser?.email.toString())
                            mFireStore.collection("friends").document(friendEmail)
                                    .set(FriendLists(friendList).updateFriendList())
                                    .addOnSuccessListener { Log.d(tag, "Successfully accepted") }
                                    .addOnFailureListener { Log.e(tag, "Failed to accept with: $it") }
                        }
                // Add friend to friend list, remove from friend wait confirm list and update to FireStore
                list.add(friendEmail)
                listWait.remove(friendEmail)
                mFireStore.collection("friends").document(mAuth.currentUser?.email.toString())
                        .set(FriendLists(false, list, listWait).toMap())
                        .addOnSuccessListener {
                            Log.d(tag, "Successfully accepted")
                            Toast.makeText(mContext, "Added", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.d(tag, "Failed to accept with: $e")
                            Toast.makeText(mContext, "Failed with: $e", Toast.LENGTH_SHORT).show()
                        }
                notifyDataSetChanged()
            }
            // Refuse: remove from wait confirm list
            view.findViewById<ImageButton>(R.id.refuse_friendRequestList).setOnClickListener {
                listWait.remove(friendEmail)
                mFireStore.collection("friends").document(mAuth.currentUser?.email.toString())
                        .update(FriendLists(false, listWait).updateFriendWaitConfirm())
                        .addOnSuccessListener {
                            Log.d(tag, "Successfully refused")
                            Toast.makeText(mContext, "Refused", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.d(tag, "Failed to refuse with: $e")
                            Toast.makeText(mContext, "Failed with: $e", Toast.LENGTH_SHORT).show()
                        }
                notifyDataSetChanged()
            }
            return view
        }
    }

}
