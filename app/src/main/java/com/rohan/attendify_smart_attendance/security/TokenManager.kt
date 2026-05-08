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
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ID_KEY = stringPreferencesKey("user_id")

        private const val MASTER_KEY_URI = "android-keystore://attendify_master_key"
        private const val TAG = "token"
    }

    private val aead: Aead

    init {
        // Initialize Google Tink Crypto
        AeadConfig.register()

        // Build the secure keyset using the hardware Android Keystore
        aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    suspend fun getUserRole(): String? = context.dataStore.data.first()[USER_ROLE_KEY]
    suspend fun getUserName(): String? = context.dataStore.data.first()[USER_NAME_KEY]
    suspend fun getUserId(): String? = context.dataStore.data.first()[USER_ID_KEY]

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

            Log.e(TAG, "CRITICAL ERROR: Failed to encrypt or save the token. Cause: ${e.message}", e)
        }
    }

    suspend fun saveUserDetails(userName:String,role:String,userId:String){

        try {
            context.dataStore.edit { prefs ->
                prefs[USER_NAME_KEY]=userName
                prefs[USER_ROLE_KEY]=role
                prefs[USER_ID_KEY]=userId
            }
            Log.i(TAG,"saved user details")

        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "failed to save user details ${e.message}")

        }
    }



    fun getAccessTokenSync(): String? {
        return runBlocking {
            val encodedToken = context.dataStore.data.first()[ACCESS_TOKEN_KEY]
                ?: return@runBlocking null

            try {
                val encryptedBytes = Base64.decode(encodedToken, Base64.DEFAULT)
                val decryptedBytes = aead.decrypt(encryptedBytes, null)
                String(decryptedBytes)
            } catch (e: Exception) {

                Log.e(TAG, "SECURITY ERROR: Failed to decrypt token. It may be corrupted. Cause: ${e.message}", e)
                null
            }
        }
    }

    // DELETE
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