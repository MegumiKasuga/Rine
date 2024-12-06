package edu.carole.rine.data.model

import java.util.UUID

/**
 * Data class that captures user information for logged in users retrieved from LoginViewModel
 */
data class LoggedInUser(
    val userId: UUID,
    val displayName: String
)