package com.example.rickandmorty.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rickandmorty.model.entity.Person
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: Person)

    @Query("SELECT * FROM local_persons")
    fun getAllImages(): Flow<List<Person>>

    @Query("SELECT * FROM local_persons")
    suspend fun getAllImagesOnce(): List<Person>

    @Query("SELECT * FROM local_persons WHERE id = :imageId")
    suspend fun getImageById(imageId: UUID): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImage(person: Person)

    @Update
    suspend fun updatePerson(person: Person)
}