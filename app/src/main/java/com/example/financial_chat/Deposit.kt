package com.example.financial_chat

import kotlin.math.pow

class Deposit(private val rate: Float) {
    private val monthlyRate = rate / 100 / 12

    fun calculate_lump_sum(goalAmount: Float, months: Int): Float {
        return goalAmount / ((1 + monthlyRate).pow(months))
    }
}