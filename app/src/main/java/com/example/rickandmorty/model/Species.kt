package com.example.rickandmorty.model

import androidx.annotation.Keep

@Keep
enum class Species {
    HUMAN, ALIEN, MYTHOLOGICAL_CREATURE, ROBOT, ANIMAL
}

fun Species.toUsual(): String {
    return when (this.toString()) {
        "HUMAN" -> "Human"
        "ALIEN" -> "Alien"
        "MYTHOLOGICAL_CREATURE" -> "Mythological creature"
        "ROBOT" -> "Robot"
        "ANIMAL" -> "Animal"
        else -> "Unknown"
    }
}