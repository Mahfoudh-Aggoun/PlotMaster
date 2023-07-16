package com.example.plotmaster.util

import android.annotation.SuppressLint
import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class GrblLookups(context: Context, prefix: String) {
    private val lookups: HashMap<String, Array<String>> = HashMap()

    init {
        val filename = "$prefix.csv"
        try {
            context.assets.open(filename).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val parts = line!!.split(",").toTypedArray()
                        lookups[parts[0]] = parts
                    }
                }
            }
        } catch (ex: IOException) {
            println("Unable to load GRBL resources.")
            ex.printStackTrace()
        }
    }

    fun lookup(idx: String): Array<String>? {
        return lookups[idx]
    }
}