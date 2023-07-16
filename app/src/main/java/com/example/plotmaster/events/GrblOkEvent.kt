package com.example.plotmaster.events

class GrblOkEvent(private var message: String) {

    fun getMessage(): String {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

}
