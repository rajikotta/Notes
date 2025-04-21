package com.raji.notes.database.repository

import com.raji.notes.database.models.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, ObjectId>{
    fun findByEmail(email: String): User?
}