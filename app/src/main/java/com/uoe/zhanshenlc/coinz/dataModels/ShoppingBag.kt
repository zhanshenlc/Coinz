package com.uoe.zhanshenlc.coinz.dataModels

class ShoppingBag {

    private var date: String = ""
    private var missionComplete = false
    private var meat: Long = 0
    private var bread: Long = 0
    private var water: Long = 0
    private var pill: Long = 0

    constructor(date: String) {
        this.date = date
    }

    constructor(amount: Long, good: Goods) {
        when (good) {
            Goods.MEAT -> this.meat = amount
            Goods.BREAD -> this.bread = amount
            Goods.WATER -> this.water = amount
            else -> this.pill = amount
        }
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["date"] = date
        result["missionComplete"] = missionComplete
        result["meat"] = meat
        result["bread"] = bread
        result["water"] = water
        result["pill"] = pill
        return result
    }

    fun update(good: Goods): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        when (good) {
            Goods.MEAT -> result["meat"] = meat
            Goods.BREAD -> result["bread"] = bread
            Goods.WATER -> result["water"] = water
            else -> result["pill"] = pill
        }
        return result
    }

}