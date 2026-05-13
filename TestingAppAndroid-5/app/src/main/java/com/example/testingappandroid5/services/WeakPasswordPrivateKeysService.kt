package com.example.testingappandroid5.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.KeyPairGenerator
import java.security.PrivateKey
import javax.crypto.Cipher

/**
 * Сервис 1 - Использование хранилища ключей со слабым паролем, содержащее закрытые ключи
 * Относится к M1: Improper Credential Usage из Mobile OWASP Top10
 * 
 * УЯЗВИМОСТЬ: Слабый пароль "1234" для защиты закрытых ключей в KeyStore
 */
class WeakPasswordPrivateKeysService : Service() {

    // УЯЗВИМОСТЬ: слабый пароль для хранилища закрытых ключей
    private val weakPassword = "1234".toCharArray()
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore")
    }

    override fun onCreate() {
        super.onCreate()
        initializeKeyStore()
    }

    private fun initializeKeyStore() {
        keyStore.load(null)
        
        // Генерация пары ключей с использованием слабого пароля
        if (!keyStore.containsAlias("private_key_alias")) {
            generateKeyPair()
        }
        
        // УЯЗВИМОСТЬ: передача слабого пароля для доступа к закрытому ключу
        accessPrivateKey()
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )
        
        val parameterSpec = android.security.keystore.KeyGenParameterSpec.Builder(
            "private_key_alias",
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or 
            android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS1)
            .build()
        
        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()
        
        // УЯЗВИМОСТЬ: сохранение закрытого ключа с использованием слабого пароля
        storePrivateKeyWithWeakPassword()
    }

    // УЯЗВИМОСТЬ: метод сохранения закрытого ключа со слабым паролем
    private fun storePrivateKeyWithWeakPassword() {
        try {
            val passwordProtection = PasswordProtection(weakPassword)
            // Здесь происходит передача слабого пароля в хранилище
            // Строка уязвимости: PasswordProtection(weakPassword) где weakPassword = "1234"
            keyStore.setEntry(
                "private_key_entry",
                KeyStore.SecretKeyEntry(null),
                passwordProtection
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // УЯЗВИМОСТЬ: доступ к закрытому ключу с использованием слабого пароля
    private fun accessPrivateKey() {
        try {
            val passwordProtection = PasswordProtection(weakPassword)
            // Строка уязвимости: использование слабого пароля для получения ключа
            val entry = keyStore.getEntry("private_key_entry", passwordProtection)
            
            // Получение закрытого ключа
            val privateKey = keyStore.getKey("private_key_alias", weakPassword) as? PrivateKey
            
            // Использование ключа для шифрования
            if (privateKey != null) {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cipher.init(Cipher.ENCRYPT_MODE, privateKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
