package com.example.salmaflorist

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val PREF_NAME = "SalmaFloristSession"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_EMAIL = "userEmail"
    }

    fun createLoginSession(email: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}
