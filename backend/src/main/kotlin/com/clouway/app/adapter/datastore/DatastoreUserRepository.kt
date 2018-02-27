package com.clouway.app.adapter.datastore

import com.clouway.app.SaltedHash
import com.clouway.app.core.User
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

    override fun registerUser(user: User): Long {
        //check if there is someone registered with this username already
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        if (list.find { it.getProperty("Username").toString() == username } != null) {
        val list = datastoreTemplate.fetch("Users", null, entityMapper)
        if (list.find {
                    it.getProperty("Username").toString() == user.username ||
                            it.getProperty("Email").toString() == user.email
                } != null) {
            return -1L
        }
        //register this user
        val saltedHash = SaltedHash(30, user.password)
        val entity = Entity("Users")
        entity.setProperty("Email", user.email)
        entity.setProperty("Username", user.username)
        entity.setProperty("Password", saltedHash.hash)
        entity.setProperty("Salt", saltedHash.salt)
        return datastore.put(entity)?.id ?: return -1L
    }

    override fun authenticateByUsername(username: String, password: String): Boolean {
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

    override fun authenticateByEmail(email: String, password: String): Boolean {
        val filter = FilterPredicate("Email", EQUAL, email)
        val salt = datastoreTemplate.fetch("Users", filter, object : EntityMapper<String> {
            override fun fetch(entity: Entity): String {
                return entity.getProperty("Salt").toString()
            }
        })
        if (salt.isEmpty()) {
            return false
        }
        val filter1 = FilterPredicate("Email", EQUAL, email)
        val filter2 = FilterPredicate("Password", EQUAL, DigestUtils.sha256Hex(salt.first() + password))
        val compositeFilter = CompositeFilter(AND, listOf(filter1, filter2))
        val list = datastoreTemplate.fetch(compositeFilter, "Users", object : EntityMapper<Entity> {
            override fun fetch(entity: Entity): Entity = entity
        })
        return !list.isEmpty()
    }

    override fun getUsername(id: Long): String? {
        val list = datastore.prepare(Query("Users")).asList(fetchOptions)
        val desiredEntity = list.find { it.key.id == id } ?: return null
        return desiredEntity.getProperty("Username").toString()
    }

    override fun getUserId(usernameOrEmail: String): Long {
        val list = datastoreTemplate.fetch("Users", null, entityMapper)
        return list.find {
            it.getProperty("Username").toString() == usernameOrEmail ||
                    it.getProperty("Email").toString() == usernameOrEmail
        }?.key?.id ?: -1L
    }
}