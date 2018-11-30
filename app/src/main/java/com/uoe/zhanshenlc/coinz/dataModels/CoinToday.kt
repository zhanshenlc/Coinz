package com.uoe.zhanshenlc.coinz.dataModels

class CoinToday {

    private var date: String = ""
    private var currencies = HashMap<String, String>()
    private var values = HashMap<String, Double>()

    constructor(date: String) {
        this.date = date
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

    fun collectCoin(coinID: String, currency: String, value: Double) {
        currencies[coinID] = currency
        values[coinID] = value
    }

    fun collected(coinID: String): Boolean { return currencies.containsKey(coinID) }

    fun fromMap(data: Map<String?, Any?>?) {
        this.date = data!!["date"].toString()
        this.currencies = data["currencies"] as HashMap<String, String>
        this.values = data["values"] as HashMap<String, Double>
    }

    fun getDate(): String { return this.date }
}