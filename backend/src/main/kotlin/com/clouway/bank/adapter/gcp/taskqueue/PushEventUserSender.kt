package com.clouway.bank.adapter.gcp.taskqueue

import com.clouway.bank.core.UserEventHandler
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class PushEventUserSender : UserEventHandler {
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