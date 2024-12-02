package com.example.financial_chat

import kotlin.math.pow

class LoanInterest(private val principal: Float, private val annualRate: Float, private val months: Int) {
    private val monthlyRate = annualRate / 100 / 12

    fun calculate(): Map<String, Float> {
        val monthlyPayment = principal * monthlyRate * (1 + monthlyRate).pow(months) / ((1 + monthlyRate).pow(months) - 1)
        val totalPayment = monthlyPayment * months
        val interestPayment = totalPayment - principal
        return mapOf(
            "monthly_payment" to monthlyPayment,
            "total_payment" to totalPayment,
            "interest_payment" to interestPayment
        )
    }
}