package com.raji.notes.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService(@Value("\${jwt.secret}") private val jwtSecret: String) {

    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret.toByteArray()))

    private val accessTokenValidityMs = 15L * 60L * 1000L
     val refreshTokenValidityMs = 30L * 60L * 1000L


    private fun generateToken(userID: String, type: String, expiry: Long): String {

        val now = Date()

        val expiryDate = Date(now.time + expiry)

        return Jwts.builder()
            .subject(userID)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userID: String): String = generateToken(userID, "access", accessTokenValidityMs)

    fun generateRefreshToken(userID: String): String = generateToken(userID, "refresh", refreshTokenValidityMs)

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String): String {
        val claims = parseAllClaims(token) ?: throw IllegalArgumentException("Invalid token.")
        return claims.subject
    }


    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}