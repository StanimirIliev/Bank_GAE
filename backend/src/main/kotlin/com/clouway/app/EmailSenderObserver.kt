package com.clouway.app

import com.clouway.app.core.Observer
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.RetryOptions
import com.google.appengine.api.taskqueue.TaskOptions

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class EmailSenderObserver : Observer {
    override fun onRegister(email: String, username: String) {
        val queue = QueueFactory.getQueue("default")
        queue.add(
                TaskOptions
                        .Builder
                        .withMethod(TaskOptions.Method.POST)
                        .url("/tasks/emailSender")
                        .payload("email=$email&username=$username")
        )
    }

    override fun onLogin(username: String) {}

    override fun onLogout(username: String) {}
}