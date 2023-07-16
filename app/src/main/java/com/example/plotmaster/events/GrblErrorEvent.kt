package com.example.plotmaster.events

import com.example.plotmaster.GrblController
import com.example.plotmaster.R
import com.example.plotmaster.util.GrblLookups

class GrblErrorEvent(private val lookups: GrblLookups, private val message: String) {

    private var errorCode: Int = 0
    private var errorName: String = ""
    private var errorDescription: String = ""

    init {
        val inputParts = message.split(":")
        if(inputParts.size == 2) {
            val lookup = lookups.lookup(inputParts[1].trim())
            if(lookup != null){
                errorCode = lookup[0].toInt()
                errorName = lookup[1]
                errorDescription = lookup[2]
            }
        }
    }

    override fun toString(): String {
        return GrblController.getInstance()!!
            .getString(R.string.text_grbl_error_format, errorCode, errorDescription)
    }

    fun getMessage(): String {
        return message
    }

    fun getErrorCode(): Int {
        return errorCode
    }

    fun getErrorName(): String {
        return errorName
    }

    fun getErrorDescription(): String {
        return errorDescription
    }
}