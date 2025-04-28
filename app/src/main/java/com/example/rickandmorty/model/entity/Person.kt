package com.example.rickandmorty.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rickandmorty.model.Species
import com.example.rickandmorty.model.Status
import java.util.UUID

@Entity(tableName = "local_persons")
data class Person(
    @PrimaryKey val id: UUID,
    val name: String,
    val status: Status,
    val species: Species,
    val filePath: String,
    val isLoading: Boolean = false
)