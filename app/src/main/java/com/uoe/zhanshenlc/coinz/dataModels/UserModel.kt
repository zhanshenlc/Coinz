package com.uoe.zhanshenlc.coinz.dataModels

class UserModel {

    private var userID: String
    private var email: String
    private var name: String = ""
    private var friends = HashMap<String, String>()

    constructor(userID: String, email: String) {
        this.userID = userID
        this.email = email
    }

    constructor(userID: String, email: String, name: String, friends: HashMap<String, String>) {
        this.userID = userID
        this.email = userID
        this.name = name
        this.friends = friends
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["userID"] = userID
        result["email"] = email
        result["name"] = name
        result["friends"] = friends
        return result
    }
}
