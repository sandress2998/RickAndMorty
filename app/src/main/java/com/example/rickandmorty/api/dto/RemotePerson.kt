package com.example.rickandmorty.api.dto

import androidx.annotation.Keep
import com.example.rickandmorty.model.Status
import com.example.rickandmorty.model.Species
import java.util.UUID

@Keep
data class RemotePerson (
    val id: UUID,
    val name: String,
    val status: Status,
    val species: Species,
    val imageUrl: String
) {
    constructor() : this(UUID.randomUUID(), "", Status.UNKNOWN, Species.HUMAN, "") // Явный конструктор без аргументов
}