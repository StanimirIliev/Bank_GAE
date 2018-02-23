package com.clouway.app

import com.clouway.app.core.Error
import com.clouway.app.core.RequestValidator
import java.util.*

class CompositeValidator(private vararg var validators: RegexValidationRule) : RequestValidator {
    override fun validate(params: Map<String, String>): List<Error> {
        return validators.mapNotNullTo(LinkedList()) { it.validate(params) }
    }
}