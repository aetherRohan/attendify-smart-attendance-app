package com.rohan.attendify_smart_attendance.security

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


private val Context.dataStore by preferencesDataStore(name = "secure_session_store")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("jwt_access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private const val MASTER_KEY_URI = "android-keystore://attendify_master_key"
        private const val TAG = "TokenManager"
    }

    private val aead: Aead

    init {
        // 2. Initialize Google Tink Crypto
        AeadConfig.register()

        // 3. Build the secure keyset using the hardware Android Keystore
        aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    // --- Vault Operations ---

    // SAVE: Must be a suspend function because DataStore is strictly asynchronous
    suspend fun saveAccessToken(token: String) {
        try {
            // Encrypt the token with Tink, then encode it to Base64 to save as a String
            val encryptedToken = aead.encrypt(token.toByteArray(), null)
            val encodedToken = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

            context.dataStore.edit { prefs ->
                prefs[ACCESS_TOKEN_KEY] = encodedToken
            }
            Log.i(TAG, "Token successfully encrypted and saved to vault.")
        } catch (e: Exception) {
            // CRITICAL: Log the error stack trace so you know exactly what broke,
            // but NEVER log the 'token' variable itself.
            Log.e(TAG, "CRITICAL ERROR: Failed to encrypt or save the token. Cause: ${e.message}", e)
        }
    }

    // READ: Synchronous version specifically designed for the OkHttp Interceptor
    fun getAccessTokenSync(): String? {
        return runBlocking {
            val encodedToken = context.dataStore.data.first()[ACCESS_TOKEN_KEY]
                ?: return@runBlocking null

            try {
                val encryptedBytes = Base64.decode(encodedToken, Base64.DEFAULT)
                val decryptedBytes = aead.decrypt(encryptedBytes, null)
                String(decryptedBytes)
            } catch (e: Exception) {
                // If decryption fails (e.g., Keystore corrupted, OS updated), we log it
                // and return null. Returning null safely forces the user to log in again.
                Log.e(TAG, "SECURITY ERROR: Failed to decrypt token. It may be corrupted. Cause: ${e.message}", e)
                null
            }
        }
    }

    // DELETE: Burn the token on logout
    suspend fun clearTokens() {
        try {
            context.dataStore.edit { prefs ->
                prefs.clear()
            }
            Log.i(TAG, "Vault cleared successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR: Failed to clear the vault. Cause: ${e.message}", e)
        }
    }
}