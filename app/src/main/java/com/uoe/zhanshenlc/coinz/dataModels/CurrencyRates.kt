package com.uoe.zhanshenlc.coinz.dataModels

import java.util.*
import kotlin.collections.HashMap

class CurrencyRates {

    var date: String
    // Random Numbers in case no valid rate inputs (although almost impossible)
    var dolrToGold = 7.0 + 4 * Random().nextDouble()
    var shilToGold = 5.0 + 3 * Random().nextDouble()
    var quidToGold = 9.0 + 5 * Random().nextDouble()
    var penyToGold = 2.0 + 2 * Random().nextDouble()

    constructor(date: String) {
        this.date = date
    }

    constructor(date: String, dolrToGold: Double, shilToGold: Double, quidToGold: Double, penyToGold: Double) {
        this.date = date
        this.dolrToGold = dolrToGold
        this.shilToGold = shilToGold
        this.quidToGold = quidToGold
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

    fun dateConvert(): String {
        // Input date format: YYYY/MM/DD
        val year = date.substring(0, 4)
        val month = date.substring(5, 7)
        val day = date.substring(8, 10)
        val monthString = when (month) {
            "01" -> "Jan"
            "02" -> "Feb"
            "03" -> "Mar"
            "04" -> "Apr"
            "05" -> "May"
            "06" -> "Jun"
            "07" -> "Jul"
            "08" -> "Aug"
            "09" -> "Sep"
            "10" -> "Oct"
            "11" -> "Nov"
            "12" -> "Dec"
            else -> ""
        }
        return "$monthString $day $year"
    }

    // jsonFrom is the date in form: "DOW MMM DD YYYY" DOW: day of week
    // example: "Mon Jan 01 2018"
    fun dateCheck(jsonForm: String): Boolean {
        return jsonForm.contains(dateConvert())
    }

}