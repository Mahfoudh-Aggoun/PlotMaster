package com.example.plotmaster.events

class GrblRealTimeCommandEvent(private val command: Byte) {
    fun getCommand(): Byte {
        return command
    }
}