package com.uoe.zhanshenlc.coinz.dataModels

class FriendLists  {

    private var friendList = ArrayList<String>()
    private var friendWaitConfirm = ArrayList<String>()
    private var newRequest: Boolean = false

    constructor()

    constructor(friendList: ArrayList<String>) {
        this.friendList = friendList
    }

    constructor(newRequest: Boolean, friendWaitConfirm: ArrayList<String>) {
        this.newRequest = newRequest
        this.friendWaitConfirm = friendWaitConfirm
    }

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

    fun updateFriendList(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["friendList"] = friendList
        return result
    }

    fun updateFriendWaitConfirm(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["newRequest"] = newRequest
        result["friendWaitConfirm"] = friendWaitConfirm
        return result
    }

}