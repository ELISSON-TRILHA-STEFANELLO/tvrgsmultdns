package com.tvrgs.domain.manager

interface ParentalPinVerifier {
    suspend fun verifyParentalPin(pin: String): Boolean
}