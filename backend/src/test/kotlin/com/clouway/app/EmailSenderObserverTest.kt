package com.clouway.app

import com.clouway.app.core.EmailSender
import org.apache.log4j.Logger
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test

class EmailSenderObserverTest {

    @Rule
    @JvmField
    val context = JUnitRuleMockery()
    private val sender = context.mock(EmailSender::class.java)
    private val logger = Logger.getLogger("EmailSenderObserverTest")
    private val observer = EmailSenderObserver(sender, logger)

    @Test
    fun assertThatOnRegisterItSendsEmail() {
        context.checking(object : Expectations() {
            init {
                oneOf(sender).setFrom("e.corp@bank.com")
                will(returnValue(sender))
                oneOf(sender).addTo("someone@example.com")
                will(returnValue(sender))
                oneOf(sender).setSubject("Registration in bank of E corp")
                will(returnValue(sender))
                oneOf(sender).setText("Hello user123. Welcome to bank of E corp. Thank you for choosing us.")
                will(returnValue(sender))
                oneOf(sender).send()
            }
        })
        observer.onRegister("someone@example.com", "user123")
    }
}