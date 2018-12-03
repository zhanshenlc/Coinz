package com.uoe.zhanshenlc.coinz.dataModels

import java.util.*
import kotlin.collections.HashMap

class CurrencyRates {

    var date: String
    var dolrToGold = 7.0 + 4 * Random().nextDouble()
    var quidToGold = 9.0 + 5 * Random().nextDouble()
    var shilToGold = 5.0 + 3 * Random().nextDouble()
    var penyToGold = 2.0 + 2 * Random().nextDouble()

    constructor(date :String) {
        this.date = date
    }

    constructor(date: String, dolrToGold: Double, quidToGold: Double, shilToGold: Double,
                penyToGold: Double) {
        this.date = date
        this.dolrToGold = dolrToGold
        this.quidToGold = quidToGold
        this.shilToGold = shilToGold
        this.penyToGold = penyToGold
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["date"] = date
        result["dolrToGold"] = dolrToGold
        result["quidToGold"] = quidToGold
        result["shilToGold"] = shilToGold
        result["penyToGold"] = penyToGold
        return result
    }

}