package com.example.plotmaster.events

class UiToastEvent(private var message: String) {
    var longToast: Boolean = false
    var isWarning: Boolean = false

    constructor(message: String, longToast: Boolean) : this(message) {
        this.longToast = longToast
    }

    constructor(message: String, longToast: Boolean, isWarning: Boolean) : this(message) {
        this.longToast = longToast
        this.isWarning = isWarning
    }

    fun getMessage(): String {
        return this.message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getIsWarning(): Boolean {
        return this.isWarning
    }

    fun setIsWarning(isWarning: Boolean) {
        this.isWarning = isWarning
    }
}
