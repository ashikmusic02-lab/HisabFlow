package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

data class UserPreferencesState(
    val userName: String = "",
    val currencyCode: String = "BDT (৳)",
    val language: String = "en", // "en" or "bn"
    val isDarkTheme: Boolean = false,
    val isPrivacyHidden: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isUnlocked: Boolean = false,
    val remainingLockoutSeconds: Int = 0
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hisabflow_secure_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<UserPreferencesState> = _state.asStateFlow()

    private fun loadInitialState(): UserPreferencesState {
        val pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
        val remaining = calculateRemainingLockoutSeconds()
        return UserPreferencesState(
            userName = prefs.getString(KEY_USER_NAME, "") ?: "",
            currencyCode = prefs.getString(KEY_CURRENCY, "BDT (৳)") ?: "BDT (৳)",
            language = prefs.getString(KEY_LANGUAGE, "en") ?: "en",
            isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false),
            isPrivacyHidden = prefs.getBoolean(KEY_PRIVACY_HIDDEN, false),
            isPinEnabled = pinEnabled,
            // If PIN is enabled, app starts locked; otherwise starts unlocked
            isUnlocked = !pinEnabled,
            remainingLockoutSeconds = remaining
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
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hash = hashPinWithSalt(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .putInt(KEY_LOCKOUT_DURATION_SECONDS, 30)
            .apply()

        _state.value = _state.value.copy(
            isPinEnabled = true,
            isUnlocked = true,
            remainingLockoutSeconds = 0
        )
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .putInt(KEY_LOCKOUT_DURATION_SECONDS, 30)
            .apply()

        _state.value = _state.value.copy(
            isPinEnabled = false,
            isUnlocked = true,
            remainingLockoutSeconds = 0
        )
    }

    fun calculateRemainingLockoutSeconds(): Int {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        return if (lockoutUntil > now) {
            ((lockoutUntil - now + 999) / 1000).toInt()
        } else {
            0
        }
    }

    fun updateLockoutState(): Int {
        val remaining = calculateRemainingLockoutSeconds()
        if (_state.value.remainingLockoutSeconds != remaining) {
            _state.value = _state.value.copy(remainingLockoutSeconds = remaining)
        }
        return remaining
    }

    fun verifyPin(pin: String): Boolean {
        val remaining = calculateRemainingLockoutSeconds()
        if (remaining > 0) {
            _state.value = _state.value.copy(remainingLockoutSeconds = remaining)
            return false
        }

        val savedSalt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val savedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""

        val inputHash = if (savedSalt.isNotEmpty()) {
            hashPinWithSalt(pin, savedSalt)
        } else {
            // Backward compatibility fallback
            legacyHash(pin)
        }

        val isValid = savedHash.isNotEmpty() && savedHash == inputHash

        if (isValid) {
            // Reset fail counter and lockouts on success
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .putInt(KEY_LOCKOUT_DURATION_SECONDS, 30)
                .apply()

            _state.value = _state.value.copy(
                isUnlocked = true,
                remainingLockoutSeconds = 0
            )
            return true
        } else {
            // Handle failure
            val failedCount = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val currentDuration = prefs.getInt(KEY_LOCKOUT_DURATION_SECONDS, 30).coerceAtLeast(30)

            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedCount)

            var newRemaining = 0
            if (failedCount >= 5) {
                val lockoutUntil = System.currentTimeMillis() + (currentDuration * 1000L)
                val nextDuration = (currentDuration * 2).coerceAtMost(3600) // double wait time
                editor.putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
                editor.putInt(KEY_LOCKOUT_DURATION_SECONDS, nextDuration)
                newRemaining = currentDuration
            }
            editor.apply()

            _state.value = _state.value.copy(remainingLockoutSeconds = newRemaining)
            return false
        }
    }

    fun lockApp() {
        if (_state.value.isPinEnabled) {
            _state.value = _state.value.copy(
                isUnlocked = false,
                remainingLockoutSeconds = calculateRemainingLockoutSeconds()
            )
        }
    }

    private fun hashPinWithSalt(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun legacyHash(pin: String): String {
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
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_FAILED_ATTEMPTS = "key_pin_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "key_pin_lockout_until"
        private const val KEY_LOCKOUT_DURATION_SECONDS = "key_pin_lockout_duration_sec"
    }
}
