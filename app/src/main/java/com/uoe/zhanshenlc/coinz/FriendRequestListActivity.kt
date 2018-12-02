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

        val listView: ListView = findViewById(R.id.listView_friendRequestList)
        fireStore.collection("friends").document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    Log.d(tag, "User friends data found.")
                    val friendList = it.data!!["friendList"] as HashMap<String, String>
                    val friendWaitConfirm = it.data!!["friendWaitConfirm"] as HashMap<String, String>
                    listView.adapter = MyCustomAdapter(this, friendList, friendWaitConfirm,
                            fireStore, mAuth)
                }

        val toolbar: Toolbar = findViewById(R.id.toolbar_friendRequestList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.addFriend_friendRequestList).setOnClickListener {
            startActivity(Intent(applicationContext, AddFriendActivity::class.java))
        }
    }

    private class MyCustomAdapter(context: Context, friendList: HashMap<String, String>, friendWaitConfirm: HashMap<String, String>,
                                  fireStore: FirebaseFirestore, auth: FirebaseAuth): BaseAdapter() {

        private val mContext: Context = context
        private val mFireStore = fireStore
        private val mAuth = auth
        private var list = friendList
        private var listWait = friendWaitConfirm

        private val tag = "FriendRequestListActivity"

        override fun getCount(): Int {
            return listWait.keys.toList().size
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

            val friendEmail = listWait.keys.toList()[position]
            val friendID = listWait[friendEmail]
            view.findViewById<TextView>(R.id.email_friendRequestList).text = friendEmail
            mFireStore.collection("users").document(friendID!!).get()
                    .addOnSuccessListener {
                        Log.d(tag, "Get $friendEmail data success")
                        val name = it.data!!["name"] as String
                        view.findViewById<TextView>(R.id.name_friendRequestList).text = name
                    }
                    .addOnFailureListener { e -> Log.d(tag, "Failed to get $friendEmail data with: $e") }
            view.findViewById<ImageButton>(R.id.accept_friendRequestList).setOnClickListener {
                mFireStore.collection("friends").document(friendEmail).get()
                        .addOnSuccessListener {
                            Log.d(tag, "Get $friendEmail friends data success")
                            val newRequest = it.data!!["newRequest"] as Boolean
                            val friendList = it.data!!["friendList"] as HashMap<String, String>
                            val friendWaitConfirm = it.data!!["friendWaitConfirm"] as HashMap<String, String>
                            friendList[mAuth.currentUser?.email.toString()] = mAuth.uid.toString()
                            mFireStore.collection("friends").document(friendEmail)
                                    .set(FriendLists(newRequest, friendList, friendWaitConfirm).toMap())
                                    .addOnSuccessListener { Log.d(tag, "Successfully accepted") }
                                    .addOnFailureListener { e -> Log.d(tag, "Failed to accept with: $e") }
                        }
                list[friendEmail] = friendID
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
            view.findViewById<ImageButton>(R.id.refuse_friendRequestList).setOnClickListener {
                listWait.remove(friendEmail)
                mFireStore.collection("friends").document(mAuth.currentUser?.email.toString())
                        .set(FriendLists(false, list, listWait).toMap())
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
