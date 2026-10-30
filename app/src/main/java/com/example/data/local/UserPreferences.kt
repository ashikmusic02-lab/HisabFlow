package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

data class UserPreferencesState(
    val userName: String = "SF Farhad",
    val currencyCode: String = "SAR (﷼)",
    val language: String = "en", // "en" or "bn"
    val isDarkTheme: Boolean = false,
    val isPrivacyHidden: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isUnlocked: Boolean = false
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hisabflow_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<UserPreferencesState> = _state.asStateFlow()

    private fun loadInitialState(): UserPreferencesState {
        val pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
        return UserPreferencesState(
            userName = prefs.getString(KEY_USER_NAME, "SF Farhad") ?: "SF Farhad",
            currencyCode = prefs.getString(KEY_CURRENCY, "SAR (﷼)") ?: "SAR (﷼)",
            language = prefs.getString(KEY_LANGUAGE, "en") ?: "en",
            isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false),
            isPrivacyHidden = prefs.getBoolean(KEY_PRIVACY_HIDDEN, false),
            isPinEnabled = pinEnabled,
            // If PIN is enabled, app starts locked; otherwise starts unlocked
            isUnlocked = !pinEnabled
        )
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _state.value = _state.value.copy(userName = name)
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
        _state.value = _state.value.copy(currencyCode = currency)
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _state.value = _state.value.copy(language = lang)
    }

    fun toggleLanguage() {
        val nextLang = if (_state.value.language == "bn") "en" else "bn"
        setLanguage(nextLang)
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _state.value = _state.value.copy(isDarkTheme = enabled)
    }

    fun togglePrivacy() {
        val next = !_state.value.isPrivacyHidden
        prefs.edit().putBoolean(KEY_PRIVACY_HIDDEN, next).apply()
        _state.value = _state.value.copy(isPrivacyHidden = next)
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
        _state.value = _state.value.copy(isPinEnabled = true, isUnlocked = true)
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
        _state.value = _state.value.copy(isPinEnabled = false, isUnlocked = true)
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        val inputHash = hashPin(pin)
        val valid = savedHash.isNotEmpty() && savedHash == inputHash
        if (valid) {
            _state.value = _state.value.copy(isUnlocked = true)
        }
        return valid
    }

    fun lockApp() {
        if (_state.value.isPinEnabled) {
            _state.value = _state.value.copy(isUnlocked = false)
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _state.value = UserPreferencesState()
    }

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_DARK_THEME = "key_dark_theme"
        private const val KEY_PRIVACY_HIDDEN = "key_privacy_hidden"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
    }
}
