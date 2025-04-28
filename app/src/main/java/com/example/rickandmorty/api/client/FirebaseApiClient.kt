package com.example.rickandmorty.api.client

import com.example.rickandmorty.api.dto.RemotePerson
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseApiClient {
    val db = Firebase.firestore

    suspend fun getPersons(): List<RemotePerson> = suspendCoroutine { continuation ->
        db.collection("characters")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val characters = querySnapshot.documents.map { doc ->
                    val documentId = doc.id // Это строка, если нужен UUID, преобразуем
                    val person = doc.toObject(RemotePerson::class.java)!!

                    // Добавляем ID документа в объект RemotePerson
                    person.copy(id = UUID.fromString(documentId)) // Преобразуем ID в UUID
                        //doc.toObject(RemotePerson::class.java)!!
                }

                continuation.resume(characters)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }
}
