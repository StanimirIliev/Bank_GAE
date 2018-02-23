package com.clouway.app.core

interface RequestValidator {
    fun validate(params: Map<String, String>): List<Error>
}