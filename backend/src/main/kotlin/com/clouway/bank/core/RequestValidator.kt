package com.clouway.bank.core

interface RequestValidator {
    fun validate(params: Map<String, String>): List<Error>
}