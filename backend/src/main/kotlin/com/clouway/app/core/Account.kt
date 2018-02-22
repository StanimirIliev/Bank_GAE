package com.clouway.app.core

data class Account(val title: String, val userId: Long, val currency: Currency, val balance: Float, var id: Long = -1L)