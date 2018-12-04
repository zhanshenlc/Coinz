package com.uoe.zhanshenlc.coinz.dataModels

class FriendLists  {

    private var friendList = ArrayList<String>()
    private var friendWaitConfirm = ArrayList<String>()
    private var newRequest: Boolean = false

    constructor()

    constructor(newRequest: Boolean, friendList: ArrayList<String>, friendWaitConfirm: ArrayList<String>) {
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