package com.example.financial_chat

import kotlin.math.pow

class Saving(private val rate: Float) {
    private val monthlyRate = rate / 100 / 12

    fun calculate_monthly_saving(monthlySaving: Float, months: Int): Float {
        return monthlySaving * ((1 + monthlyRate).pow(months) - 1) / monthlyRate
    }

    fun calculate_goal_amount(goalAmount: Float, months: Int): Float {
        return goalAmount * monthlyRate / ((1 + monthlyRate).pow(months) - 1)
    }
}