package com.clouway.app.adapter.datastore

import com.clouway.app.RegistrationObserver
import com.clouway.app.SaltedHash
import com.clouway.app.core.User
import com.clouway.app.core.UserRepository
import com.clouway.app.datastore.core.DatastoreTemplate
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Query.CompositeFilter
import com.google.appengine.api.datastore.Query.CompositeFilterOperator.AND
import com.google.appengine.api.datastore.Query.FilterOperator.EQUAL
import com.google.appengine.api.datastore.Query.FilterPredicate
import org.apache.commons.codec.digest.DigestUtils

class DatastoreUserRepository(
        private val datastoreTemplate: DatastoreTemplate,
        private val observer: RegistrationObserver
) : UserRepository {

    private val entityMapper = object : EntityMapper<Entity> {
        override fun fetch(entity: Entity): Entity = entity
    }

    override fun registerUser(user: User): Long {
        //check if there is someone registered with this username already
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
        val key = datastoreTemplate.insert(entity)
        if (key != null) {
            observer.onRegister(user.email, user.username)
            return key.id
        }
        return -1L
    }

    override fun authenticateByUsername(username: String, password: String): Boolean {
        val filter = FilterPredicate("Username", EQUAL, username)
        val salt = datastoreTemplate.fetch("Users", filter, object : EntityMapper<String> {
            override fun fetch(entity: Entity): String {
                return entity.getProperty("Salt").toString()
            }
        })
        if (salt.isEmpty()) {
            return false
        }
        val filter1 = FilterPredicate("Username", EQUAL, username)
        val filter2 = FilterPredicate("Password", EQUAL, DigestUtils.sha256Hex(salt.first() + password))
        val compositeFilter = CompositeFilter(AND, listOf(filter1, filter2))
        val list = datastoreTemplate.fetch(compositeFilter, "Users", object : EntityMapper<Entity> {
            override fun fetch(entity: Entity): Entity = entity
        })
        return !list.isEmpty()
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
        val list = datastoreTemplate.fetch("Users", null, entityMapper)
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