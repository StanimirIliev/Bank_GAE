package com.clouway.app.core

interface RequestValidator {
    fun validate(params: Map<String, Array<String>>): List<Error>
}