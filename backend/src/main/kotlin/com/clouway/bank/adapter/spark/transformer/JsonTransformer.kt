package com.clouway.bank.adapter.spark.transformer

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import spark.ResponseTransformer
import java.time.LocalDateTime

class JsonTransformer : ResponseTransformer {

    private val gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
                override fun read(`in`: JsonReader): LocalDateTime {
                    val value = `in`.nextString()
                    return LocalDateTime.parse(value)
                }

                override fun write(out: JsonWriter, value: LocalDateTime) {
                    out.value(value.toString())
                }
            })
            .create()

    override fun render(model: Any?): String {
        return gson.toJson(model)
    }
}