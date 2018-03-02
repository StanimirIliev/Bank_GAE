package com.clouway.bank.adapter.gcp.transaction

import com.clouway.bank.adapter.security.hashing.SaltedHash
import com.clouway.bank.core.User
import com.clouway.bank.core.UserEventHandler
import com.clouway.bank.core.Users
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class SafeUsersProxy(
        private val origin: Users,
        private val datastore: DatastoreService,
        private val userEventHandler: UserEventHandler
) : Users {
    override fun registerUser(user: User): Long {
        val transaction = datastore.beginTransaction()
        val saltedHash = SaltedHash(30, user.password)
        val entity = Entity("Users")
        entity.setProperty("Email", user.email)
        entity.setProperty("Username", user.username)
        entity.setProperty("Password", saltedHash.hash)
        entity.setProperty("Salt", saltedHash.salt)
        try {
            val id = datastore.put(transaction, entity)?.id ?: -1L
            transaction.commit()
            if (id != -1L) {
                userEventHandler.onRegister(user.email, user.username)
            }
            return id
        } catch (e: Exception) {
            transaction.rollback()
            return -1L
        }
    }

    override fun authenticateByUsername(username: String, password: String): Boolean {
        return origin.authenticateByUsername(username, password)
    }

    override fun authenticateByEmail(email: String, password: String): Boolean {
        return origin.authenticateByEmail(email, password)
    }

    override fun getUsername(id: Long): String? {
        return origin.getUsername(id)
    }

    override fun getUserId(usernameOrEmail: String): Long {
        return origin.getUserId(usernameOrEmail)
    }
}