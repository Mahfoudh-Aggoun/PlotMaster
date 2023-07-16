package com.example.plotmaster.model

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class Position(val cordX: Double, val cordY: Double, val cordZ: Double) {

    private fun roundDouble(value: Double): Double {
        val s = decimalFormat.format(value)
        return s.toDouble()
    }

    fun getCordxString() : String {return cordX.toString()}
    fun getCordYString() : String {return cordY.toString()}

    fun hasChanged(position: Position): Boolean {
        return position.cordX.compareTo(cordX) != 0 || position.cordY.compareTo(cordY) != 0 || position.cordZ.compareTo(
            cordZ
        ) != 0
    }

    fun atZero(): Boolean {
        return Math.abs(cordX) == 0.0 && Math.abs(cordY) == 0.0 && Math.abs(
            cordZ
        ) == 0.0
    }

    companion object {
        private val numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH)
        private val decimalFormat = numberFormat as DecimalFormat
    }

    init {
        decimalFormat.applyPattern("#0.###")
    }
}