package com.uoe.zhanshenlc.coinz

import com.uoe.zhanshenlc.coinz.dataModels.CurrencyRates
import org.junit.Test
import org.junit.Assert.*

class DateConversionTest {

    @Test
    fun test_dateConvert_1() {
        val date = "2018/02/01"
        assertEquals("Feb 01 2018", CurrencyRates(date).dateConvert())
    }

    @Test
    fun test_dateConvert_2() {
        val date = "2018/04/30"
        assertEquals("Apr 30 2018", CurrencyRates(date).dateConvert())
    }

    @Test
    fun test_dateConvert_3() {
        val date = "2018/03/05"
        assertEquals("Mar 05 2018", CurrencyRates(date).dateConvert())
    }

    @Test
    fun test_dateConvert_4() {
        val date = "2019/07/10"
        assertEquals("Jul 10 2019", CurrencyRates(date).dateConvert())
    }

    @Test
    fun test_dateConvert_5() {
        val date = "2010/12/01"
        assertEquals("Dec 01 2010", CurrencyRates(date).dateConvert())
    }

}