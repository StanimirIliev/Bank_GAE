package com.clouway.email.sender.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

interface SimpleEmailSender {
    fun setTo(email: String) : SimpleEmailSender
    fun setFrom(email: String) : SimpleEmailSender
    fun setSubject(subject: String) : SimpleEmailSender
    fun setContent(content: String,type: String?) : SimpleEmailSender
    fun send()
}