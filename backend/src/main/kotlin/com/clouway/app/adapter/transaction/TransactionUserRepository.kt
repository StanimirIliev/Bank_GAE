package com.clouway.app.adapter.transaction

import com.clouway.app.SaltedHash
import com.clouway.app.core.Observer
import com.clouway.app.core.User
import com.clouway.app.core.UserRepository
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class TransactionUserRepository(
        private val origin: UserRepository,
        private val datastore: DatastoreService,
        private val observer: Observer
) : UserRepository {
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
                observer.onRegister(user.email, user.username)
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