package com.clouway.bank.adapter.validation

import com.clouway.bank.core.User
import com.clouway.bank.core.Users
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
class ValidationUsersProxy(private val origin: Users, private val datastore: DatastoreService): Users {
    override fun registerUser(user: User): Long {
        //check if there is someone registered with this username already
        val list = datastore.prepare(Query("Users")).asList(FetchOptions.Builder.withDefaults())
        if (list.find {
                    it.getProperty("Username").toString() == user.username ||
                            it.getProperty("Email").toString() == user.email
                } != null) {
            return -1L
        }
        return origin.registerUser(user)
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