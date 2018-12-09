package com.uoe.zhanshenlc.coinz.dataModels

class BankAccount {

    private var quid: Double = 0.0
    private var shil: Double = 0.0
    private var dolr: Double = 0.0
    private var peny: Double = 0.0
    private var gold: Double = 0.0

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["quid"] = quid
        result["shil"] = shil
        result["dolr"] = dolr
        result["peny"] = peny
        result["gold"] = gold
        return result
    }

}