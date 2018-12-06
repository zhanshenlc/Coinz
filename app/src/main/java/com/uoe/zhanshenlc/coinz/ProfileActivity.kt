package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private val fireStore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        fireStore.collection("users")
                .document(mAuth.currentUser?.email.toString()).get()
                .addOnSuccessListener {
                    findViewById<TextView>(R.id.name_profile).text = it.data!!["name"] as String
                    findViewById<TextView>(R.id.email_profile).text = it.data!!["email"] as String
                }

        findViewById<Button>(R.id.nameBtn_profile).setOnClickListener {
            val newName = findViewById<EditText>(R.id.inputName_profile).text.toString()
            if (!newName.isEmpty()) {
                val result = HashMap<String, Any>()
                result["name"] = newName
                fireStore.collection("users")
                        .document(mAuth.currentUser?.email.toString())
                        .update(result.toMap())
                        .addOnSuccessListener {  }
                        .addOnFailureListener {  }
                findViewById<TextView>(R.id.name_profile).text = newName
                Toast.makeText(this, "Name change success", Toast.LENGTH_SHORT).show()
            } else {
                findViewById<EditText>(R.id.inputName_profile).error = "Name cannot be empty."
            }
        }

        findViewById<Button>(R.id.passwordBtn_profile).setOnClickListener{
            val oldPasswordView = findViewById<EditText>(R.id.oldPassword_profile)
            val newPasswordView = findViewById<EditText>(R.id.newPassword_profile)
            val confirmPasswordView = findViewById<EditText>(R.id.confirmPassword_profile)
            when {
                oldPasswordView.text.isEmpty() -> oldPasswordView.error = "Old password cannot be empty."
                newPasswordView.text.isEmpty() -> newPasswordView.error = "New password cannot be empty."
                confirmPasswordView.text.isEmpty() -> confirmPasswordView.error = "Confirm password cannot be empty."
                newPasswordView.text.toString().length < 6 -> newPasswordView.error = "New password too short."
                newPasswordView.text.toString() != confirmPasswordView.text.toString() ->
                        confirmPasswordView.error = "Two passwords are not identical."
                else -> {
                    mAuth.currentUser?.reauthenticate(EmailAuthProvider.getCredential(mAuth.currentUser?.email.toString(),
                            oldPasswordView.text.toString()))
                            ?.addOnSuccessListener {
                                mAuth.currentUser?.updatePassword(newPasswordView.text.toString())
                                Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
                            }
                }
            }
        }

        findViewById<TextView>(R.id.forgetPassword_profile).setOnClickListener {
            startActivity(Intent(applicationContext, ForgetPasswordActivity::class.java))
        }
    }
}
