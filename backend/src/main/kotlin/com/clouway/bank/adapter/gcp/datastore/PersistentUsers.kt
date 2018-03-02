package com.clouway.bank.adapter.gcp.datastore

import com.clouway.bank.adapter.security.hashing.SaltedHash
import com.clouway.bank.core.User
import com.clouway.bank.core.UserEventHandler
import com.clouway.bank.core.Users
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Query.CompositeFilter
import com.google.appengine.api.datastore.Query.CompositeFilterOperator.AND
import com.google.appengine.api.datastore.Query.FilterOperator.EQUAL
import com.google.appengine.api.datastore.Query.FilterPredicate
import org.apache.commons.codec.digest.DigestUtils

class PersistentUsers(private val datastore: DatastoreService, private val userEventHandler: UserEventHandler) : Users {

    private val fetchOptions = FetchOptions.Builder.withDefaults()

    override fun registerUser(user: User): Long {
        val saltedHash = SaltedHash(30, user.password)
        val entity = Entity("Users")
        entity.setProperty("Email", user.email)
        entity.setProperty("Username", user.username)
        entity.setProperty("Password", saltedHash.hash)
        entity.setProperty("Salt", saltedHash.salt)
        val id = datastore.put(entity)?.id ?: -1L
        if(id != -1L) {
            userEventHandler.onRegister(user.email, user.username)
        }
        return id
    }

    override fun authenticateByUsername(username: String, password: String): Boolean {
        val filter = FilterPredicate("Username", EQUAL, username)
        var query = Query("Users")
        query.filter = filter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        if (entityList.isEmpty()) {
            return false
        }
        val salt = entityList.first().getProperty("Salt").toString()
        val filter1 = FilterPredicate("Username", EQUAL, username)
        val filter2 = FilterPredicate("Password", EQUAL, DigestUtils.sha256Hex(salt + password))
        val compositeFilter = CompositeFilter(AND, listOf(filter1, filter2))
        query = Query("Users")
        query.filter = compositeFilter
        return !datastore.prepare(query).asList(fetchOptions).isEmpty()
    }

    override fun authenticateByEmail(email: String, password: String): Boolean {
        val filter = FilterPredicate("Email", EQUAL, email)
        var query = Query("Users")
        query.filter = filter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        if (entityList.isEmpty()) {
            return false
        }
        val salt = entityList.first().getProperty("Salt").toString()
        val filter1 = FilterPredicate("Email", EQUAL, email)
        val filter2 = FilterPredicate("Password", EQUAL, DigestUtils.sha256Hex(salt + password))
        val compositeFilter = CompositeFilter(AND, listOf(filter1, filter2))
        query = Query("Users")
        query.filter = compositeFilter
        return !datastore.prepare(query).asList(fetchOptions).isEmpty()
    }

    override fun getUsername(id: Long): String? {
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        val desiredEntity = list.find { it.key.id == id } ?: return null
        return desiredEntity.getProperty("Username").toString()
    }

    override fun getUserId(usernameOrEmail: String): Long {
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        return list.find {
            it.getProperty("Username").toString() == usernameOrEmail ||
                    it.getProperty("Email").toString() == usernameOrEmail
        }?.key?.id ?: -1L
    }
}