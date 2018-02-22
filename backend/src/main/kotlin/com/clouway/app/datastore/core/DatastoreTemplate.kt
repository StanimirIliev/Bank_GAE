package com.clouway.app.datastore.core

import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Query.CompositeFilter
import com.google.appengine.api.datastore.Query.FilterPredicate

interface DatastoreTemplate {
    /**
     * Insert entity in the datastore
     * @param entity The entity you want to insert
     * @return the key of the new entity or null if the insertion was unsuccessful
     */
    fun insert(entity: Entity): Key?

    /**
     * Update entity from the datastore
     * @param entity The entity with its new properties
     * @return true if the entity was updated false if was not
     */
    fun update(entity: Entity): Boolean

    /**
     * Delete entity from the datastore
     * @param key The key of the entity you want to delete
     */
    fun delete(key: Key): Boolean

    /**
     * Fetch Objects of the datastore
     * @param kind The kind of the object
     * @param filter Filter for the query. If null is passed no filter is used
     * @return list with all filtered objects
     */
    fun <T> fetch(kind: String, filter: FilterPredicate?, mapper: EntityMapper<T>): List<T>

    /**
     * Fetch Objects of the datastore
     * @param compositeFilter Composite filter for the query
     * @param kind The kind of the object
     * @return list with all filtered objects
     */
    fun <T> fetch(compositeFilter: CompositeFilter, kind: String, mapper: EntityMapper<T>): List<T>
}