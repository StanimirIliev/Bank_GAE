package com.clouway.app.core

import java.io.Serializable
import java.time.LocalDateTime

data class Session(val userId: Long, val createdOn: LocalDateTime, val expiresAt: LocalDateTime): Serializable