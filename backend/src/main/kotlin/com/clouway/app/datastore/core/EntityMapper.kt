package com.clouway.app.datastore.core

import com.google.appengine.api.datastore.Entity

interface EntityMapper<T> {
    fun fetch(entity: Entity): T
}