package com.uoe.zhanshenlc.coinz.dataModels

class CoinToday {

    private var date: String = ""
    private var currencies = HashMap<String, String>()
    private var values = HashMap<String, Double>()
    private var inBankCoinIDToday = ArrayList<String>()
    private var purchasedCoinIDToday = ArrayList<String>()
    private var sentCoinIDToday = ArrayList<String>()

    constructor(date: String) {
        this.date = date
    }

    constructor(currencies: HashMap<String, String>, values: HashMap<String, Double>) {
        this.currencies = currencies
        this.values = values
    }

    constructor(currencies: HashMap<String, String>, values: HashMap<String, Double>, sentCoinIDToday: ArrayList<String>) {
        this.currencies = currencies
        this.values = values
        this.sentCoinIDToday = sentCoinIDToday
    }

    constructor(date: String, currencies: HashMap<String, String>, values: HashMap<String, Double>,
                inBankCoinIDToday: ArrayList<String>, purchasedCoinIDToday: ArrayList<String>,
                sentCoinIDToday: ArrayList<String>) {
        this.date = date
        this.currencies = currencies
        this.values = values
    }

    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["date"] = date
        result["currencies"] = currencies
        result["values"] = values
        result["inBankCoinIDToday"] = inBankCoinIDToday
        result["purchasedCoinIDToday"] = purchasedCoinIDToday
        result["sentCoinIDToday"] = sentCoinIDToday
        return result
    }

    fun updateCollection(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["currencies"] = currencies
        result["values"] = values
        return result
    }

    fun updateSend(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["currencies"] = currencies
        result["values"] = values
        result["sentCoinIDToday"] = sentCoinIDToday
        return result
    }

}
