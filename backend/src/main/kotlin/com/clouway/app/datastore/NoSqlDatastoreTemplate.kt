package com.clouway.app.datastore

import com.clouway.app.datastore.core.DatastoreTemplate
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.*
import com.google.appengine.api.datastore.Query.CompositeFilter
import com.google.appengine.api.datastore.Query.FilterPredicate
import java.util.*

class NoSqlDatastoreTemplate(private val datastore: DatastoreService) : DatastoreTemplate {
    override fun insert(entity: Entity): Key? {
        try {
            val entitiesCountBefore = datastore.prepare(Query(entity.kind)).asList(FetchOptions.Builder.withDefaults()).size
            val key = datastore.put(entity)
            val entitiesCountAfter = datastore.prepare(Query(entity.kind)).asList(FetchOptions.Builder.withDefaults()).size
            if (entitiesCountAfter > entitiesCountBefore) {
                return key
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    override fun update(entity: Entity): Boolean {
        try {
            val entitiesListBefore = datastore.prepare(Query(entity.kind)).asList(FetchOptions.Builder.withDefaults())
            val entityBefore = entitiesListBefore.find { it.key == entity.key }!!
            datastore.put(entity)
            val entitiesListAfter = datastore.prepare(Query(entity.kind)).asList(FetchOptions.Builder.withDefaults())
            val entityAfter = entitiesListAfter.find { it.key == entity.key }!!
            if (entityBefore.properties != entityAfter.properties) {
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    override fun delete(key: Key): Boolean {
        try {
            val entitiesCountBefore = datastore.prepare(Query(key.kind)).asList(FetchOptions.Builder.withDefaults()).size
            datastore.delete(key)
            val entitiesCountAfter = datastore.prepare(Query(key.kind)).asList(FetchOptions.Builder.withDefaults()).size
            if (entitiesCountAfter < entitiesCountBefore) {
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    override fun <T> fetch(kind: String, filter: FilterPredicate?, mapper: EntityMapper<T>): List<T> {
        return try {
            val query = Query(kind)
            if (filter != null) {
                query.filter = filter
            }
            val entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults())
            val list = LinkedList<T>()
            entities.forEach {
                list.add(mapper.fetch(it))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun <T> fetch(compositeFilter: CompositeFilter, kind: String, mapper: EntityMapper<T>): List<T> {
        return try {
            val query = Query(kind)
            query.filter = compositeFilter
            val entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults())
            val list = LinkedList<T>()
            entities.forEach {
                list.add(mapper.fetch(it))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}