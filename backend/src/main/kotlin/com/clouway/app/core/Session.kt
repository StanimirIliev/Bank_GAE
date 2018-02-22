package com.clouway.app.core

import java.time.LocalDateTime

data class Session(val userId: Long, val createdOn: LocalDateTime, val expiresAt: LocalDateTime)