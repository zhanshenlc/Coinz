package com.uoe.zhanshenlc.coinz

import com.uoe.zhanshenlc.coinz.dataModels.CurrencyRates
import org.junit.Test
import org.junit.Assert.*

class DateCheckTest {

    @Test
    fun test_dateCheck_1() {
        val date = "Fri Dec 07 2018"
        assertEquals(true, CurrencyRates("2018/12/07").dateCheck(date))
    }

    @Test
    fun test_dateCheck_2() {
        val date = "Thu Dec 06 2018"
        assertEquals(false, CurrencyRates("2118/12/06").dateCheck(date))
    }

    @Test
    fun test_dateCheck_3() {
        val date = "Wed Dec 12 2018"
        assertEquals(true, CurrencyRates("2018/12/12").dateCheck(date))
    }

    @Test
    fun test_dateCheck_4() {
        val date = "Sat Nov 04 2018"
        assertEquals(true, CurrencyRates("2018/11/04").dateCheck(date))
    }

    @Test
    fun test_dateCheck_51() {
        val date = "Mon Dec 03 2018"
        assertEquals(false, CurrencyRates("2018/12/07").dateCheck(date))
    }

}