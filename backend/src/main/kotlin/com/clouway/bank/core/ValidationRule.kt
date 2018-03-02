package com.clouway.bank.core

interface ValidationRule {
    fun validate(params: Map<String, String>): Error?
}