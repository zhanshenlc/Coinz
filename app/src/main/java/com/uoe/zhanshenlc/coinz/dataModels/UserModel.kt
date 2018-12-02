package com.uoe.zhanshenlc.coinz.dataModels

class UserModel (u: String, e: String, n: String) {

    private var userID: String = u
    private var email: String = e
    private var name: String = n

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["userID"] = userID
        result["email"] = email
        result["name"] = name
        return result
    }
}
