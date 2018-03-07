package com.clouway.emailing.adapter.event

import com.clouway.eventbus.core.Event

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

data class UserLoginEvent(val username: String) : Event