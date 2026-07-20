package de.gyrosbande.dice.domain

/** A member of the Gyrosbande. [id] is the Room row id (0 = not saved yet). */
data class Player(
    val id: Long = 0,
    val name: String,
    /** Only active players take part in a round ("spielt mit"). */
    val isActive: Boolean = true,
)
