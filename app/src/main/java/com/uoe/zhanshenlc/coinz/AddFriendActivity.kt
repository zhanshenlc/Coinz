package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.FriendLists

class AddFriendActivity : AppCompatActivity() {

    private val tag = "AddFriendActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        val toolbar: Toolbar = findViewById(R.id.toolbar_addFriend)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.addFriendbtn_addFriend).setOnClickListener {
            val inputEmailView = findViewById<TextView>(R.id.inputEmail_addFriend)
            val inputEmail = inputEmailView.text.toString()
            when(inputEmail) {
                String() -> inputEmailView.error = "Please input an email."
                mAuth.currentUser!!.email.toString() -> inputEmailView.error = "You cannot add yourself."
                else -> {
                    fireStore.collection("friends").document(inputEmail).get()
                            .addOnSuccessListener {
                                when(it!!.data) {
                                    null -> inputEmailView.error = "User does not exist."
                                    else -> {
                                        Log.d(tag, "User found.")
                                        val friendList = it.data!!["friendList"] as ArrayList<String>
                                        val friendWaitConfirm = it.data!!["friendWaitConfirm"] as ArrayList<String>
                                        if (friendList.contains(mAuth.currentUser?.email.toString())) {
                                            inputEmailView.error = "Already in your friend list."
                                        } else {
                                            friendWaitConfirm.add(mAuth.currentUser?.email.toString())
                                            fireStore.collection("friends").document(inputEmail)
                                                    .set(FriendLists(true, friendList, friendWaitConfirm).toMap())
                                                    .addOnSuccessListener { Log.d(tag, "Request sent") }
                                                    .addOnFailureListener { e -> Log.d(tag, "Fail to sent request with: $e") }
                                            Toast.makeText(this, "Request sent.", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    }
                                }
                            }
                }
            }
        }
    }

}
