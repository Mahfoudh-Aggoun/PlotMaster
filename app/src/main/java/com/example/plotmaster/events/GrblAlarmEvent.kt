package com.example.plotmaster.events

import com.example.plotmaster.GrblController
import com.example.plotmaster.R
import com.example.plotmaster.util.GrblLookups

class GrblAlarmEvent(private val lookups: GrblLookups, private val message: String) {
    private var alarmCode = 0
    private var alarmName: String? = null
    private var alarmDescription: String? = null

    init {
        val inputParts = message.split(":")
        if (inputParts.size == 2) {
            val lookup = lookups.lookup(inputParts[1].trim())
            if (lookup != null) {
                alarmCode = lookup[0].toInt()
                alarmName = lookup[1]
                alarmDescription = lookup[2]
            }
        }
    }

    override fun toString(): String {
        return GrblController.getInstance()!!
            .getString(R.string.text_grbl_alarm_format, alarmCode, alarmDescription)
    }

    fun getMessage(): String {
        return message
    }

    fun getAlarmCode(): Int {
        return alarmCode
    }

    fun getAlarmName(): String? {
        return alarmName
    }

    fun getAlarmDescription(): String? {
        return alarmDescription
    }
}
