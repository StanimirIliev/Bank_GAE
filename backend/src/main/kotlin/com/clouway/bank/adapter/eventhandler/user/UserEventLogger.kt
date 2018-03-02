package com.clouway.bank.adapter.eventhandler.user

import com.clouway.bank.core.UserEventHandler
import org.apache.log4j.Logger

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class UserEventLogger(private val logger: Logger) : UserEventHandler {
    override fun onRegister(email: String, username: String) {}

    override fun onLogin(username: String) {
        logger.info("$username has logged in.")
    }

    override fun onLogout(username: String) {
        logger.info("$username has logged out.")
    }
}