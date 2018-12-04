package com.uoe.zhanshenlc.coinz.dataModels

class CoinToday {

    private var date: String = ""
    private var currencies = HashMap<String, String>()
    private var values = HashMap<String, Double>()

    constructor(date: String) {
        this.date = date
    }

    constructor(currencies: HashMap<String, String>, values: HashMap<String, Double>) {
        this.currencies = currencies
        this.values = values
    }

    constructor(date: String, currencies: HashMap<String, String>, values: HashMap<String, Double>) {
        this.date = date
        this.currencies = currencies
        this.values = values
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["date"] = date
        result["currencies"] = currencies
        result["values"] = values
        return result
    }

    fun update(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["currencies"] = currencies
        result["values"] = values
        return result
    }

}