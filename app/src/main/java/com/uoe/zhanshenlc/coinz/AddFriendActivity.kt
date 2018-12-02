package com.uoe.zhanshenlc.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.FriendLists

class AddFriendActivity : AppCompatActivity() {

    private val tag = "AddFriendActivity"
    val mAuth = FirebaseAuth.getInstance()
    val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        val inputEmail = findViewById<TextView>(R.id.inputEmail_addFriend).text.toString()
        findViewById<ImageButton>(R.id.addFriendbtn_addFriend).setOnClickListener {
            if (inputEmail == mAuth.currentUser!!.email.toString()) {
                Toast.makeText(this, "Cannot add yourself!", Toast.LENGTH_SHORT).show()
            } else {
                fireStore.collection("userList").document("users")
                        .get().addOnSuccessListener { task ->
                            if (task.data != null) { Log.d(tag, "No users currently") }
                            else {
                                val userList = task.data!!["List"] as HashMap<String, String>
                                if (inputEmail.isEmpty()) {
                                    Toast.makeText(this, "You should input a valid email.", Toast.LENGTH_SHORT).show()
                                } else if (userList[inputEmail] != null) {
                                    Log.d(tag, "User found. Adding user as friend.")
                                    val friendUid = userList[inputEmail] as String
                                    fireStore.collection("friends").document(friendUid)
                                            .get().addOnSuccessListener { task ->
                                                val friendList = task.data!!["friendList"] as ArrayList<String>
                                                val friendRequested = task.data!!["friendRequested"] as ArrayList<String>
                                                val friendWaitConfirm = task.data!!["friendWaitConfirm"] as ArrayList<String>
                                                friendWaitConfirm.add(mAuth.currentUser!!.email.toString())
                                                fireStore.collection("friends").document(friendUid)
                                                        .set(FriendLists(true, friendList, friendRequested,
                                                                friendWaitConfirm).toMap())
                                                        .addOnSuccessListener {  }
                                                        .addOnFailureListener {  }
                                            }
                                            .addOnFailureListener {  }
                                    fireStore.collection("friends").document(mAuth.uid.toString())
                                            .get().addOnSuccessListener{ task ->
                                                val newRequest = task.data!!["newRequest"] as Boolean
                                                val friendList = task.data!!["friendList"] as ArrayList<String>
                                                val friendRequested = task.data!!["friendRequested"] as ArrayList<String>
                                                val friendWaitConfirm = task.data!!["friendWaitConfirm"] as ArrayList<String>
                                                friendRequested.add(mAuth.currentUser!!.email.toString())
                                                fireStore.collection("friends").document(mAuth.uid.toString())
                                                        .set(FriendLists(newRequest, friendList, friendRequested, friendWaitConfirm).toMap())
                                                        .addOnSuccessListener {  }
                                                        .addOnFailureListener {  }
                                            }
                                            .addOnFailureListener {  }
                                    finish()
                                } else { Toast.makeText(this, "No user with this email.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                        .addOnFailureListener { e -> Log.d(tag, "Failed to get user list with: $e") }
            }
        }
    }
}
