package com.clouway.app

import com.clouway.app.adapter.datastore.DatastoreAccountRepository
import com.clouway.app.adapter.datastore.DatastoreSessionRepository
import com.clouway.app.adapter.datastore.DatastoreTransactionRepository
import com.clouway.app.adapter.datastore.DatastoreUserRepository
import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.delete.RemoveAccountRoute
import com.clouway.app.adapter.http.get.*
import com.clouway.app.adapter.http.post.*
import com.clouway.app.adapter.memcache.CachedSessions
import com.clouway.app.adapter.transaction.TransactionAccountRepository
import com.clouway.app.adapter.transaction.TransactionTransactionRepository
import com.clouway.app.adapter.validation.ValidationAccountRepository
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.memcache.MemcacheServiceFactory
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.apache.log4j.Logger
import spark.Spark.*
import java.io.File
import java.io.FileReader

class ConfiguredServer {
    fun start() {
        val sendgridApiKey = FileReader("sendgrid.env").readText()

        val logger = Logger.getLogger("ConfiguredServer")
        val config = Configuration(Configuration.VERSION_2_3_23)
        val file = File(ConfiguredServer::class.java.getResource("freemarker/templates/").file)
        config.setDirectoryForTemplateLoading(file)
        config.defaultEncoding = "UTF-8"
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        config.logTemplateExceptions = false

        val datastore = DatastoreServiceFactory.getDatastoreService()

        val sessionRepository = CachedSessions(
                DatastoreSessionRepository(datastore),
                MemcacheServiceFactory.getMemcacheService()
        )
        val transactionRepository = TransactionTransactionRepository(
                DatastoreTransactionRepository(datastore),
                datastore
        )
        val accountRepository = ValidationAccountRepository(
                TransactionAccountRepository(
                        DatastoreAccountRepository(datastore, transactionRepository),
                        datastore,
                        transactionRepository
                ),
                datastore
        )
        val emailSender = Sendgrid("https://api.sendgrid.com", sendgridApiKey)
        val mainObserver = MainObserver(
                EmailSenderObserver(),
                LogsObserver(logger)
        )
        val userRepository = DatastoreUserRepository(datastore)
        val compositeValidator = CompositeValidator(
                RegexValidationRule(
                        "email",
                        "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
                        "Incorrect email.\n"
                ),
                RegexValidationRule(
                        "username",
                        "[a-zA-Z\\d]{4,15}",
                        "Incorrect username.\nShould be between 4 and 15 characters long " +
                                "and to not contain special symbols.\n"
                ),
                RegexValidationRule("password",
                        "^[a-zA-Z\\d]{6,30}\$",
                        "Incorrect password.\nShould be between 6 and 30 characters long, " +
                                "and to not contain special symbols."
                ))

        initExceptionHandler { e -> logger.fatal("Unable to start the server", e) }
        internalServerError { _, res ->
            res.redirect("/static/index/images/500-wallpaper.jpg")
        }

        val transformer = JsonTransformer()
        post("/tasks/emailSender", RegistrationEmailSendingRoute(emailSender, logger))
        get("/index", IndexPageRoute())
        post("/login", LoginUserHandler(userRepository, sessionRepository, mainObserver, config))
        get("/login", LoginPageRoute(config))
        get("/registration", RegistrationPageRoute(config))
        post("/registration", RegisterUserHandler(userRepository, sessionRepository, mainObserver, compositeValidator, config))
        get("/home", Secured(sessionRepository, HomePageRoute(), logger))
        get("/logout", LogoutRoute(sessionRepository, userRepository, mainObserver, logger), transformer)
        path("/v1") {
            path("/accounts") {
                get("", Secured(sessionRepository, AccountsListRoute(accountRepository), logger), transformer)
                get("/:id", Secured(sessionRepository, AccountDetailsRoute(accountRepository), logger), transformer)
                post("", Secured(sessionRepository, NewAccountRoute(accountRepository), logger), transformer)
                post("/:id/deposit", Secured(sessionRepository, DepositRoute(accountRepository), logger), transformer)
                post("/:id/withdraw", Secured(sessionRepository, WithdrawRoute(accountRepository), logger), transformer)
                delete("/:id", Secured(sessionRepository, RemoveAccountRoute(accountRepository, logger), logger), transformer)
            }
            get("/activity", ActivityRoute(sessionRepository), transformer)
            get("/username", Secured(sessionRepository, UsersRoute(userRepository), logger), transformer)
            get("/transactions/:param", Secured(sessionRepository, TransactionsRoute(transactionRepository,
                    accountRepository), logger), transformer)
        }
        path("/cron") {
            get("/remove-expired-sessions", RemoveExpiredSessionsRoute(sessionRepository, logger))
        }
        get("/*") { _, res -> res.redirect("/home") }
    }

}
