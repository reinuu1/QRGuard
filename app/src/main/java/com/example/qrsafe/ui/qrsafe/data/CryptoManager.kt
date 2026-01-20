package com.example.qrsafe.ui.qrsafe.data

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"

    // Transformăm parola utilizatorului într-o cheie secretă
    private fun getKey(password: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    // Funcția de CRIPTARE
    fun encrypt(plainText: String, password: String): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getKey(password)
            val iv = ByteArray(16)
            java.security.SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray())

            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            // Formatul nostru secret: "QRSAFE:IV:DATA"
            return "QRSAFE:$ivString:$encryptedString"
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // Funcția de DECRIPTARE
    fun decrypt(encryptedFullString: String, password: String): String? {
        try {
            if (!encryptedFullString.startsWith("QRSAFE:")) return null

            val parts = encryptedFullString.split(":")
            if (parts.size != 3) return null

            val ivString = parts[1]
            val encryptedDataString = parts[2]

            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            val encryptedData = Base64.decode(encryptedDataString, Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getKey(password)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val decryptedBytes = cipher.doFinal(encryptedData)

            return String(decryptedBytes)
        } catch (e: Exception) {
            return null
        }
    }
}