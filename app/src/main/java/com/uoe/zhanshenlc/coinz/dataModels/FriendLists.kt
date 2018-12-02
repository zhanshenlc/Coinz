package com.uoe.zhanshenlc.coinz.dataModels

class FriendLists  {

    private var friendList = HashMap<String, String>()
    private var friendWaitConfirm = HashMap<String, String>()
    private var newRequest: Boolean = false

    constructor()

    constructor(newRequest: Boolean, friendList: HashMap<String, String>,
                friendWaitConfirm: HashMap<String, String>) {
        this.newRequest = newRequest
        this.friendList = friendList
        this.friendWaitConfirm = friendWaitConfirm
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["newRequest"] = newRequest
        result["friendList"] = friendList
        result["friendWaitConfirm"] = friendWaitConfirm
        return result
    }
}