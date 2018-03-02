package com.clouway.bank.adapter.gcp.datastore

import com.google.appengine.api.datastore.Entity

interface EntityMapper<T> {
    fun fetch(entity: Entity): T
}