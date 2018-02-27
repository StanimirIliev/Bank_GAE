package com.clouway.app.adapter.datastore

import com.clouway.app.SaltedHash
import com.clouway.app.core.UserRepository
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Query.CompositeFilter
import com.google.appengine.api.datastore.Query.CompositeFilterOperator.AND
import com.google.appengine.api.datastore.Query.FilterOperator.EQUAL
import com.google.appengine.api.datastore.Query.FilterPredicate
import org.apache.commons.codec.digest.DigestUtils

class DatastoreUserRepository(private val datastore: DatastoreService) : UserRepository {

    private val fetchOptions = FetchOptions.Builder.withDefaults()

    override fun registerUser(username: String, password: String): Long {
        //check if there is someone registered with this username already
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        if (list.find { it.getProperty("Username").toString() == username } != null) {
            return -1L
        }
        //register this user
        val saltedHash = SaltedHash(30, password)
        val entity = Entity("Users")
        entity.setProperty("Username", username)
        entity.setProperty("Password", saltedHash.hash)
        entity.setProperty("Salt", saltedHash.salt)
        return datastore.put(entity)?.id ?: return -1L
    }

    override fun authenticate(username: String, password: String): Boolean {
        val filter = FilterPredicate("Username", EQUAL, username)
        var query = Query("Users")
        query.filter = filter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        if(entityList.isEmpty()) {
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

    override fun getUsername(id: Long): String? {
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        val desiredEntity = list.find { it.key.id == id } ?: return null
        return desiredEntity.getProperty("Username").toString()
    }

    override fun getUserId(username: String): Long {
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        return list.find { it.getProperty("Username").toString() == username }?.key?.id ?: -1L
    }
}