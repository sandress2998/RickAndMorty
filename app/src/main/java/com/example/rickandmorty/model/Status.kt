package com.example.rickandmorty.model

import androidx.annotation.Keep

@Keep
enum class Status {
    ALIVE, DEAD, UNKNOWN
}

fun Status.toUsual(): String {
    return when (this.toString()) {
        "ALIVE" -> "Alive"
        "DEAD" -> "Dead"
        "UNKNOWN" -> "Unknown"
        else -> "Unknown"
    }
}