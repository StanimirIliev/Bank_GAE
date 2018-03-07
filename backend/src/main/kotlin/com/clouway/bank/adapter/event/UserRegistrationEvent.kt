package com.clouway.bank.adapter.event

import com.clouway.eventdispatch.core.Event

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

data class UserRegistrationEvent(val email: String, val username: String): Event