package com.example.financial_chat

class FunctionCalling {
    companion object {
        fun handleModelOutput(modelOutput: String): String {
            return try {
                val functionName = extractFunctionName(modelOutput)
                val parameters = extractParameters(modelOutput)
                callFunction(functionName, parameters)
            } catch (e: Exception) {
                e.printStackTrace()
                "요청을 처리할 수 없습니다. 다시 시도해주세요."
            }
        }

        // 함수 호출 로직
        fun callFunction(functionName: String, parameters: Map<String, Any>): String {
            return when (functionName) {
                "calculate_goal_amount_saving" -> {
                    val rate = (parameters["rate"] as? Float) ?: 0f
                    val goalAmount = (parameters["goal_amount"] as? Float) ?: 0f
                    val months = (parameters["months"] as? Int) ?: 0
                    val savingCalculator = Saving(rate)
                    "매달 저축해야 할 금액은 ${savingCalculator.calculate_goal_amount(goalAmount, months)}원 입니다."
                }
                "calculate_lump_sum_deposit" -> {
                    val rate = (parameters["rate"] as? Float) ?: 0f
                    val goalAmount = (parameters["goal_amount"] as? Float) ?: 0f
                    val months = (parameters["months"] as? Int) ?: 0
                    val depositCalculator = Deposit(rate)
                    "목표 금액을 달성하려면 초기 예치금으로 ${depositCalculator.calculate_lump_sum(goalAmount, months)}원을 예치해야 합니다."
                }
                "calculate_monthly_saving" -> {
                    val rate = (parameters["rate"] as? Float) ?: 0f
                    val monthlySaving = (parameters["monthly_saving"] as? Float) ?: 0f
                    val months = (parameters["months"] as? Int) ?: 0
                    val savingCalculator = Saving(rate)
                    "총 적립 금액은 ${savingCalculator.calculate_monthly_saving(monthlySaving, months)}원 입니다."
                }
                "calculate_loan_interest" -> {
                    val principal = (parameters["principal"] as? Float) ?: 0f
                    val annualRate = (parameters["annual_rate"] as? Float) ?: 0f
                    val months = (parameters["months"] as? Int) ?: 0
                    val loanCalculator = LoanInterest(principal, annualRate, months)
                    val result = loanCalculator.calculate()
                    "월 상환액: ${result["monthly_payment"]}원, 총 상환액: ${result["total_payment"]}원, 이자 총액: ${result["interest_payment"]}원 입니다."
                }
                else -> "요청을 이해하지 못했습니다."
            }
        }

        private fun extractFunctionName(modelOutput: String): String {
            return modelOutput.substringAfter("FunctionCall:").substringBefore(" ")
        }

        // 모델 출력에서 매개변수 추출
        private fun extractParameters(modelOutput: String): Map<String, Any> {
            val paramString = modelOutput.substringAfter("Parameters:").trim()
            return paramString.split(",").associate {
                val (key, value) = it.split(":").map { it.trim() }
                key to parseParameterValue(value)
            }
        }

        // 매개변수 값 파싱
        private fun parseParameterValue(value: String): Any {
            return when {
                value.startsWith("\"") && value.endsWith("\"") -> value.trim('"')
                value.contains(".") -> value.toDoubleOrNull() ?: value
                else -> value.toIntOrNull() ?: value
            }
        }
    }
}
