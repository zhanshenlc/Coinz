package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.BankAccount
import com.uoe.zhanshenlc.coinz.dataModels.FriendLists
import com.uoe.zhanshenlc.coinz.dataModels.UserModel

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val tag = "RegisterActivity"
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<Button>(R.id.btn_register).setOnClickListener(this)
        findViewById<TextView>(R.id.loginLink_register).setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.btn_register -> createAccount(findViewById<EditText>(R.id.inputEmail_register).text.toString(),
                    findViewById<EditText>(R.id.inputPassword_register).text.toString(),
                    findViewById<EditText>(R.id.inputName_register).text.toString())
            R.id.loginLink_register -> {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun createAccount(email: String, password: String, name: String) {
        Log.d(tag, "createAccount: $email")
        if (!validateForm()) {
            return
        }
        // [START create_user_with_email]
        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(tag, "createUserWithEmail:success")
                mAuth?.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            Log.d(tag, "Verification email sent.")
                            val fireStore = FirebaseFirestore.getInstance()
                            // Create user data
                            fireStore.collection("users").document(email)
                                    .set(UserModel(mAuth?.currentUser?.email.toString(), name).toMap())
                                    .addOnSuccessListener { Log.d(tag, "New user data successfully created.") }
                                    .addOnFailureListener{ e -> Log.w(tag, "Error creating user data with: $e") }
                            // Create bank account
                            fireStore.collection("bank accounts").document(mAuth?.uid.toString())
                                    .set(BankAccount().toMap())
                                    .addOnSuccessListener { Log.d(tag, "New bank account successfully created.") }
                                    .addOnFailureListener{ e -> Log.w(tag, "Error creating bank account with: $e") }
                            // Create friend list
                            fireStore.collection("friends").document(email)
                                    .set(FriendLists().toMap())
                                    .addOnSuccessListener { Log.d(tag, "New friend list successfully created.") }
                                    .addOnFailureListener { e -> Log.w(tag, "Error creating friend list with: $e") }
                            mAuth?.signOut()
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }
                        ?.addOnFailureListener { e -> Log.d(tag, "Error sending verification email with: $e") }
            } else {
                // If sign in fails, display a message to the user.
                Log.w(tag, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, task.exception.toString(), Toast.LENGTH_LONG).show()
                findViewById<EditText>(R.id.inputEmail_register).text.clear()
                findViewById<EditText>(R.id.inputPassword_register).text.clear()
            }
        }
        // [END create_user_with_email]
    }

    // Check whether inputs are in valid form
    private fun validateForm(): Boolean {
        var valid = true

        val name = findViewById<EditText>(R.id.inputName_register)
        if (TextUtils.isEmpty(name.text.toString())) {
            name.error = "Required."
            valid = false
        } else { name.error = null }

        val email = findViewById<EditText>(R.id.inputEmail_register)
        if (TextUtils.isEmpty(email.text.toString())) {
            email.error = "Required."
            valid = false
        } else { email.error = null }

        val password = findViewById<EditText>(R.id.inputPassword_register)
        if (TextUtils.isEmpty(password.text.toString())) {
            password.error = "Required."
            valid = false
        } else { password.error = null }

        return valid
    }

}
