package com.example.plotmaster.events

import android.text.TextUtils
import com.example.plotmaster.util.GrblLookups
import java.util.regex.Pattern

class GrblSettingMessageEvent(private val lookups: GrblLookups, private val message: String) {
    private var setting: String? = null
    private var value: String? = null
    private var units: String? = null
    private var description: String? = null
    private var shortDescription: String? = null

    companion object {
        private val MESSAGE_REGEX = Pattern.compile("(\\$\\d+)=([^ ]*)\\s?\\(?([^)]*)?\\)?")
    }

    init {
        parse()
    }

    override fun toString(): String {
        var descriptionStr = ""
        if (!TextUtils.isEmpty(description)) {
            descriptionStr = if (!TextUtils.isEmpty(units)) {
                " ($shortDescription, $units)"
            } else {
                " ($description)"
            }
        }
        return String.format("%s = %s   %s", setting, value, descriptionStr)
    }

    fun getSetting(): String? {
        return setting
    }

    fun getUnits(): String? {
        return units
    }

    fun getValue(): String? {
        return value
    }

    fun getDescription(): String? {
        return description
    }

    private fun parse() {
        val m = MESSAGE_REGEX.matcher(message)
        if (m.find()) {
            setting = m.group(1) as String
            value = m.group(2)
            val lookup = lookups.lookup(setting!!)
            if (lookup != null) {
                units = lookup[2]
                description = lookup[3]
                shortDescription = lookup[1]
            }
        }
    }
}
