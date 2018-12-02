package com.uoe.zhanshenlc.coinz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoe.zhanshenlc.coinz.dataModels.BankAccount
import com.uoe.zhanshenlc.coinz.dataModels.FriendLists
import com.uoe.zhanshenlc.coinz.dataModels.UserModel
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val tag = "RegisterActivity"

    private var profile : ImageView? = null
    private var imageUri : Uri? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener(this)
        loginLink_register.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.btn_register -> createAccount(inputEmail_register.text.toString(), inputPassword_register.text.toString(),
                    inputName_register.text.toString())
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
                            fireStore.collection("users").document(mAuth?.uid.toString())
                                    .set(UserModel(mAuth?.uid.toString(), mAuth?.currentUser?.email.toString(), name).toMap())
                                    .addOnSuccessListener { Log.d(tag, "New user data successfully created.") }
                                    .addOnFailureListener{ e -> Log.w(tag, "Error creating user data with: $e") }
                            mAuth?.signOut()
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }
                        ?.addOnFailureListener { e -> Log.d(tag, "Error sending verification email with: $e") }
            } else {
                // If sign in fails, display a message to the user.
                Log.w(tag, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, task.exception.toString(), Toast.LENGTH_LONG).show()
                startActivity(Intent(applicationContext, RegisterActivity::class.java))
                finish()
            }
        }
        // [END create_user_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val name = inputName_register.text.toString()
        if (TextUtils.isEmpty(name)) {
            inputName_register.error = "Required."
            valid = false
        } else { inputName_register.error = null }

        val email = inputEmail_register.text.toString()
        if (TextUtils.isEmpty(email)) {
            inputEmail_register.error = "Required."
            valid = false
        } else { inputEmail_register.error = null }

        val password = inputPassword_register.text.toString()
        if (TextUtils.isEmpty(password)) {
            inputPassword_register.error = "Required."
            valid = false
        } else { inputPassword_register.error = null }

        return valid
    }

    private fun upload() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
        startActivityForResult(intent, 10)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            profile?.setImageURI(data!!.data)
            if (data != null) {
                imageUri = data.data
            }
        }
    }

}
