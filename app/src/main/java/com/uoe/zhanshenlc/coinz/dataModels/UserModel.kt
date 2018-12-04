package com.uoe.zhanshenlc.coinz.dataModels

class UserModel (e: String, n: String) {

    private var email: String = e
    private var name: String = n

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["email"] = email
        result["name"] = name
        return result
    }
}
