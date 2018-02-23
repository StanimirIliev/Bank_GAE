package com.clouway.app.core

interface ValidationRule {
    fun validate(params: Map<String, String>): Error?
}