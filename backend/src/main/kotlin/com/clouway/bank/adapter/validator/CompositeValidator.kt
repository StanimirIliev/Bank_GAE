package com.clouway.bank.adapter.validator

import com.clouway.bank.adapter.validator.regex.RegexValidationRule
import com.clouway.bank.core.Error
import com.clouway.bank.core.RequestValidator
import java.util.*

class CompositeValidator(private vararg var validators: RegexValidationRule) : RequestValidator {
    override fun validate(params: Map<String, String>): List<Error> {
        return validators.mapNotNullTo(LinkedList()) { it.validate(params) }
    }
}