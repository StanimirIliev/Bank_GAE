package com.clouway.bank.core

import java.time.LocalDateTime


data class Transaction(val userId: Long, val accountId: Long, val onDate: LocalDateTime, val operation: Operation, val amount: Float)