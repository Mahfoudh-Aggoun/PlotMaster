package com.example.plotmaster.events

import com.example.plotmaster.model.Position

class GrblProbeEvent(private val probeString: String) {

    private lateinit var probePosition: Position
    private var isProbeSuccess = false

    init {
        parseProbeString()
    }

    private fun parseProbeString() {
        val parts = probeString.split(":")
        val coordinates = parts[0].split(",")

        probePosition = Position(coordinates[0].toDouble(), coordinates[1].toDouble(), coordinates[2].toDouble())
        isProbeSuccess = parts[1] == "1"
    }

    fun getIsProbeSuccess(): Boolean = isProbeSuccess

    fun getProbeCordX(): Double = probePosition.cordX

    fun getProbeCordY(): Double = probePosition.cordY

    fun getProbeCordZ(): Double = probePosition.cordZ

    fun getProbePosition(): Position = probePosition
}

