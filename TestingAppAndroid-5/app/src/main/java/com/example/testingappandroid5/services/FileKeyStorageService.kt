package com.example.testingappandroid5.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Сервис 3 - Использование файлового хранилища ключей
 * Относится к уязвимости M9: Insecure Data Storage из Mobile OWASP Top10
 * 
 * УЯЗВИМОСТЬ: Хранение ключей в незащищенном файловом хранилище без шифрования
 */
class FileKeyStorageService : Service() {

    // УЯЗВИМОСТЬ: хранение ключей в открытом виде в файловой системе
    private val keysDirectory: File by lazy {
        File(filesDir, "keys")
    }
    
    private val privateKeyFile: File by lazy {
        File(keysDirectory, "private_key.dat")
    }
    
    private val publicKeyFile: File by lazy {
        File(keysDirectory, "public_key.dat")
    }
    
    private val secretKeyFile: File by lazy {
        File(keysDirectory, "secret_key.dat")
    }

    override fun onCreate() {
        super.onCreate()
        initializeKeyStorage()
    }

    private fun initializeKeyStorage() {
        // Создание директории для хранения ключей
        if (!keysDirectory.exists()) {
            keysDirectory.mkdirs()
        }
        
        // Генерация и сохранение ключей в файл
        if (!privateKeyFile.exists()) {
            generateAndStoreKeys()
        }
        
        // УЯЗВИМОСТЬ: чтение ключей из незащищенного файла
        readKeysFromFile()
    }

    private fun generateAndStoreKeys() {
        try {
            // Генерация пары ключей RSA
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            val keyPair = keyPairGenerator.generateKeyPair()
            
            // УЯЗВИМОСТЬ: сохранение закрытого ключа в открытый файл без шифрования
            // Строка уязвимости: FileOutputStream(privateKeyFile) - запись ключа в файл
            saveKeyToFile(keyPair.private, privateKeyFile)
            
            // УЯЗВИМОСТЬ: сохранение открытого ключа в файл
            saveKeyToFile(keyPair.public, publicKeyFile)
            
            // Генерация симметричного ключа AES
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            val secretKey = keyGenerator.generateKey()
            
            // УЯЗВИМОСТЬ: сохранение секретного ключа в файл без защиты
            // Строка уязвимости: FileOutputStream(secretKeyFile) - запись секретного ключа в файл
            saveSecretKeyToFile(secretKey, secretKeyFile)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // УЯЗВИМОСТЬ: метод сохранения ключа в файл без шифрования
    private fun saveKeyToFile(key: Any, file: File) {
        try {
            // Здесь происходит передача ключа в файловое хранилище без защиты
            // Строки уязвимости:
            // - FileOutputStream(file) - создание потока записи в файл
            // - writeObject(key) - сериализация и запись ключа в открытом виде
            FileOutputStream(file).use { fos ->
                java.io.ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(key)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // УЯЗВИМОСТЬ: метод сохранения секретного ключа в файл без шифрования
    private fun saveSecretKeyToFile(key: SecretKey, file: File) {
        try {
            // Здесь происходит передача секретного ключа в файловое хранилище
            // Строка уязвимости: FileOutputStream(file).write(key.encoded) - запись байтов ключа в файл
            FileOutputStream(file).use { fos ->
                fos.write(key.encoded)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // УЯЗВИМОСТЬ: чтение ключей из незащищенного файла
    private fun readKeysFromFile() {
        try {
            // Чтение закрытого ключа из файла
            // Строка уязвимости: FileInputStream(privateKeyFile) - чтение ключа из незащищенного файла
            if (privateKeyFile.exists()) {
                FileInputStream(privateKeyFile).use { fis ->
                    java.io.ObjectInputStream(fis).use { ois ->
                        val privateKey = ois.readObject() as? PrivateKey
                        // Использование закрытого ключа
                    }
                }
            }
            
            // Чтение секретного ключа из файла
            // Строка уязвимости: FileInputStream(secretKeyFile) - чтение секретного ключа из файла
            if (secretKeyFile.exists()) {
                FileInputStream(secretKeyFile).use { fis ->
                    val keyBytes = fis.readBytes()
                    // Использование секретного ключа
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
