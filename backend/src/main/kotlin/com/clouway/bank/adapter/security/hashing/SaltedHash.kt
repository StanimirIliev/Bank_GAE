package com.clouway.bank.adapter.security.hashing

import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SaltedHash(size: Int, private val password: String) {

    private val random = SecureRandom()
    private val numbers = IntArray(size)
    val salt: String
        get() {
            val result = StringBuilder()
            numbers.forEach { result.append(it.toChar()) }
            return result.toString()
        }
    val hash: String
        get() {
            return DigestUtils.sha256Hex(salt + password)
        }

    init {// 48-57 digits 65-90 uppercase letters 97-122 lowercase letters
        for (i in numbers.indices) {
            numbers[i] = random.nextInt(74) + 48
            while (numbers[i] in 58..64 || numbers[i] in 91..96) {
                numbers[i] = random.nextInt(74) + 48
            }
        }
    }
}