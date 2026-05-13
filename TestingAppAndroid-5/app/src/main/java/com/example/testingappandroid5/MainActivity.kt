package com.example.testingappandroid5

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start vulnerable services for testing purposes
        startService(android.content.Intent(this, services.WeakPasswordPrivateKeysService::class.java))
        startService(android.content.Intent(this, services.WeakPasswordPublicKeysService::class.java))
        startService(android.content.Intent(this, services.FileKeyStorageService::class.java))
        finish()
    }
}
