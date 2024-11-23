package com.example.financial_chat

import android.content.Context
import android.content.SharedPreferences

class SessionManager (context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun saveUserSession(email: String, userId: String) {
        val editor = prefs.edit()
        editor.putString("EMAIL", email)
        editor.putString("USER_ID", userId)
        editor.apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString("EMAIL", null)
    }

    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}