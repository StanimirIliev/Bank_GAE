package com.clouway.app

import spark.servlet.SparkApplication

class MainProgram : SparkApplication {
    override fun init() {
        ConfiguredServer().start()
    }
}