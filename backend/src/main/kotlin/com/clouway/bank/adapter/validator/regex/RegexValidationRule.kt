package com.clouway.bank.adapter.validator.regex

import com.clouway.bank.core.Error
import com.clouway.bank.core.ValidationRule

class RegexValidationRule(val param: String, val expression: String, val errorMessage: String) : ValidationRule {
    override fun validate(params: Map<String, String>): Error? {
        val value = params[param]
        if (value != null && !Regex(expression).matches(value)) {
            return Error(errorMessage)
        }
        return null
    }
}