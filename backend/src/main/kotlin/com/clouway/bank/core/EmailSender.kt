package com.clouway.bank.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

interface EmailSender {
    fun addTo(vararg emails: String): EmailSender
    fun bccTo(vararg emails: String): EmailSender
    fun ccTo(vararg emails: String): EmailSender
    fun setFrom(email: String): EmailSender
    fun setReplyTo(email: String): EmailSender
    fun setSubject(subject: String): EmailSender
    fun setText(text: String): EmailSender
    fun setHtml(html: String): EmailSender
    fun addAttachment(filename: String, content: ByteArray): EmailSender
    fun send()
}