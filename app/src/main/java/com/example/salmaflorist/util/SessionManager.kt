package com.example.salmaflorist.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * SessionManager untuk mengelola sesi user yang login
 * Menyimpan: user data, JWT token, dan status login
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val TAG = "SessionManager"
        private const val PREF_NAME = "SalmaFloristSession"

        // Keys untuk SharedPreferences
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "userEmail"
        private const val KEY_ROLE = "userRole"
        private const val KEY_TOKEN = "authToken"
    }

    /**
     * Membuat sesi login setelah berhasil login/register dari API
     * @paramUserId ID user dari API (UUID string)
     * @paramUsername Username user
     * @paramEmail Email user
     * @paramRole Role user (ADMIN/USER)
     * @paramToken JWT token untuk API requests
     */
    fun createLoginSession(
        userId: String,
        username: String,
        email: String,
        role: String,
        token: String
    ) {
        Log.d(TAG, "Creating login session for user: $username (ID: $userId, Role: $role)")

        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_TOKEN, token)
        editor.apply()

        Log.d(TAG, "Login session created successfully")
    }

    /**
     * Membuat sesi login dengan data legacy (untuk backward compatibility dengan SQLite)
     * @deprecated Gunakan createLoginSession dengan lengkap untuk API integration
     */
    @Deprecated("Use createLoginSession with all parameters")
    fun createLoginSession(email: String, role: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    /**
     * Cek apakah user sedang login
     * @return Boolean true jika sudah login
     */
    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(IS_LOGGED_IN, false)
        Log.d(TAG, "Is logged in: $loggedIn")
        return loggedIn
    }

    /**
     * Mendapatkan ID user yang sedang login
     * @return String ID user atau null jika belum login
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Mendapatkan username user yang sedang login
     * @return String username atau null jika belum login
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Mendapatkan email user yang sedang login
     * @return String email atau null jika belum login
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    /**
     * Mendapatkan role user yang sedang login
     * @return String role (ADMIN/USER) atau null jika belum login
     */
    fun getUserRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    /**
     * Mendapatkan JWT token untuk API requests
     * @return String JWT token atau null jika belum login
     */
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Cek apakah user adalah admin
     * @return Boolean true jika role adalah ADMIN
     */
    fun isAdmin(): Boolean {
        val role = getUserRole()
        return role == "ADMIN" || role == "admin"
    }

    /**
     * Logout user - hapus semua data sesi
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        editor.clear()
        editor.apply()
        Log.d(TAG, "User logged out successfully")
    }

    /**
     * Update token (untuk refresh token jika ada)
     * @paramToken JWT token baru
     */
    fun updateToken(token: String) {
        Log.d(TAG, "Updating auth token")
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }
}
