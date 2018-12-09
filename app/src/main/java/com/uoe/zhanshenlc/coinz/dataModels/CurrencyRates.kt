package com.uoe.zhanshenlc.coinz.dataModels

import java.util.*
import kotlin.collections.HashMap

class CurrencyRates (d: String) {

    var date: String = d
    var dolrToGold = 7.0 + 4 * Random().nextDouble()
    var quidToGold = 9.0 + 5 * Random().nextDouble()
    var shilToGold = 5.0 + 3 * Random().nextDouble()
    var penyToGold = 2.0 + 2 * Random().nextDouble()

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