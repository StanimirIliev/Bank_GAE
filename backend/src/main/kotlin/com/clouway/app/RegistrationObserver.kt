package com.clouway.app

import com.clouway.app.core.EmailSender
import org.apache.log4j.Logger
import java.io.IOException

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class RegistrationObserver(private val sender: EmailSender, private val logger: Logger) {
    fun onRegister(email: String, username: String) {
        try {
            sender
                    .setFrom("e.corp@bank.com")
                    .addTo(email)
                    .setSubject("Registration in bank of E corp")
                    .setText("Hello $username. Welcome to bank of E corp. Thank you for choosing us.")
                    .send()
            logger.info("Successfully sent email to $email with username $username")
        } catch (e: IOException) {
            logger.error("Unable to send email to $email with username $username")
        }
    }
}