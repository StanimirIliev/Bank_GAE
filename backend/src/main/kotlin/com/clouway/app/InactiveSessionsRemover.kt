package com.clouway.app

import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class InactiveSessionsRemover(
        private val refreshTime: Long,
        private val timeUnit: TimeUnit,
        private val sessionRepository: SessionRepository,
        private val logger: Logger
) : Runnable {
    override fun run() {
        while(true) {
            try {
                sleep(timeUnit.toMillis(refreshTime))
                val removedSessions = sessionRepository.terminateInactiveSessions(LocalDateTime.now())
                if(removedSessions > 0) {
                    logger.info("$removedSessions sessions was terminated because was inactive.")
                }
            }
            catch(e: InterruptedException) {
                logger.info("InactiveSessionsRemover was interrupted.")
                break
            }
        }
    }
}