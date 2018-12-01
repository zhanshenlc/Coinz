package com.uoe.zhanshenlc.coinz.dataModels

class BankAccount {

    private var todayDate: String
    private var inBankCoinIDToday = ArrayList<String>()
    private var purchasedCoinIDToday = ArrayList<String>()
    private var sentCoinIDToday = ArrayList<String>()
    private var quid: Double = 0.0
    private var shil: Double = 0.0
    private var dolr: Double = 0.0
    private var peny: Double = 0.0
    private var gold: Double = 0.0

    constructor(todayDate: String) {
        this.todayDate = todayDate
    }

    constructor(todayDate: String, inBankCoinIDToday: ArrayList<String>, purchasedCoinIDToday: ArrayList<String>,
                sentCoinIDToday: ArrayList<String>, quid: Double, shil: Double, dolr: Double, peny: Double,
                gold: Double) {
        this.todayDate = todayDate
        this.inBankCoinIDToday = inBankCoinIDToday
        this.purchasedCoinIDToday = purchasedCoinIDToday
        this.sentCoinIDToday = sentCoinIDToday
        this.quid = quid
        this.shil = shil
        this.dolr = dolr
        this.peny = peny
        this.gold = gold
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["todayDate"] = todayDate
        result["inBankCoinIDToday"] = inBankCoinIDToday
        result["purchasedCoinIDToday"] = purchasedCoinIDToday
        result["sentCoinIDToday"] = sentCoinIDToday
        result["quid"] = quid
        result["shil"] = shil
        result["dolr"] = dolr
        result["peny"] = peny
        result["gold"] = gold
        return result
    }
}