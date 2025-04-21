package com.raji.notes.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {

    val bCryptPasswordEncoder = BCryptPasswordEncoder()

    fun encode(password: String): String = bCryptPasswordEncoder.encode(password)

    fun matches(password: String, hash: String) = bCryptPasswordEncoder.matches(password, hash)

}