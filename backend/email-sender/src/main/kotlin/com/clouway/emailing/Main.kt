package com.clouway.emailing

import spark.servlet.SparkApplication

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
class Main : SparkApplication {
    override fun init() {
        AppBootstrap().start()
    }
}