package com.clouway.app.core

interface ValidationRule {
    fun validate(params: Map<String, Array<String>>): Error?
}