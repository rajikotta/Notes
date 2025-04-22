package com.raji.notes.security

import com.raji.notes.database.models.RefreshToken
import com.raji.notes.database.models.User
import com.raji.notes.database.repository.RefreshTokenRepository
import com.raji.notes.database.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    val jwtService: JwtService,
    val userRepository: UserRepository,
    val hashEncoder: HashEncoder,
    val refreshTokenRepository: RefreshTokenRepository

) {


    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )


    fun register(email: String, password: String): User {
        val user = userRepository.findByEmail(email.trim())
        if (user != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A user with that email already exists.")
        }
        return userRepository.save(
            User(
                email = email,
                hashedPassword = hashEncoder.encode(password)
            )
        )
    }


    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email.trim())
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.")
        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.")
        }
        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        storeRefreshToken(user.id.toHexString(), newRefreshToken)
        return TokenPair(newAccessToken, newRefreshToken)
    }


    private fun storeRefreshToken(userId: String, rawToken: String) {
        val hashedToken = hashToken(rawToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        val token = RefreshToken(
            userId = ObjectId(userId),
            hashedToken = hashedToken,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save<RefreshToken>(token)
    }

    private fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(rawToken.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }


    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
        }
        val userId = jwtService.getUserIdFromToken(refreshToken)

        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")

        }

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw ResponseStatusException(
                HttpStatusCode.valueOf(401),
                "Refresh token not recognized (maybe used or expired?)"
            )
        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)


        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        storeRefreshToken(user.id.toHexString(), newRefreshToken)
        return TokenPair(newAccessToken, newRefreshToken)
    }
}