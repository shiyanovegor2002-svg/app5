package com.example.testingappandroid5.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.cert.Certificate

/**
 * Сервис 2 - Использование хранилища ключей со слабым паролем, содержащим открытые ключи
 * Относится к M1: Improper Credential Usage из Mobile OWASP Top10
 * 
 * УЯЗВИМОСТЬ: Слабый пароль "password" для защиты хранилища с открытыми ключами
 */
class WeakPasswordPublicKeysService : Service() {

    // УЯЗВИМОСТЬ: слабый пароль для хранилища открытых ключей
    private val weakPassword = "password".toCharArray()
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore")
    }

    override fun onCreate() {
        super.onCreate()
        initializeKeyStore()
    }

    private fun initializeKeyStore() {
        keyStore.load(null)
        
        // УЯЗВИМОСТЬ: передача слабого пароля для доступа к хранилищу открытых ключей
        accessPublicKey()
    }

    // УЯЗВИМОСТЬ: метод сохранения сертификата с открытым ключом со слабым паролем
    private fun storeCertificateWithWeakPassword() {
        try {
            val passwordProtection = PasswordProtection(weakPassword)
            // Строка уязвимости: PasswordProtection(weakPassword) где weakPassword = "password"
            // Здесь происходит передача слабого пароля в хранилище для защиты открытого ключа
            keyStore.setEntry(
                "public_key_entry",
                KeyStore.SecretKeyEntry(null),
                passwordProtection
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // УЯЗВИМОСТЬ: доступ к открытому ключу с использованием слабого пароля
    private fun accessPublicKey() {
        try {
            val passwordProtection = PasswordProtection(weakPassword)
            // Строка уязвимости: использование слабого пароля для получения записи
            val entry = keyStore.getEntry("public_key_entry", passwordProtection)
            
            // Получение сертификата с открытым ключом
            val certificate = keyStore.getCertificate("public_key_alias")
            
            // Использование открытого ключа
            if (certificate != null) {
                val publicKey = certificate.publicKey
                // Логика использования открытого ключа
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
