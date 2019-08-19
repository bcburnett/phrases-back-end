package com.newlondonweb

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    routing {
        get("/json/{apiKey}") {
val apiKey = call.parameters["apiKey"]
            if(apiKey=="true") call.respond(Gson().toJson(PhraseDatabase().phrase))
            else call.respond(Gson().toJson(Phrase()))
        }
    }
}

class PhraseDatabase {
    val phrase: Phrase
        @Throws(ClassNotFoundException::class)
        get() {
            val category = Categories()
            val phrase = Phrase()
            try {
                this.connect()!!.use {
                    it.createStatement().use {
                        it.executeQuery("SELECT tablename, title FROM categories limit 1 offset abs(random()) % (select count(*) from categories)")
                            .use {
                                it.next()
                                category.table = it.getString("tablename")
                                category.title = it.getString("title")
                            }
                    }
                }
            } catch (e: SQLException) {
                System.err.println(category.table)
            }

            try {
                this.connect()!!.use {
                    it.createStatement().use {
                        it.executeQuery("SELECT phrase FROM ${category.table} limit 1 offset abs(random()) % (select count(*) from ${category.table})")
                            .use {
                                it.next()
                                phrase.category = category.title
                                phrase.phrase = it.getString("phrase")
                                return phrase
                            }
                    }
                }
            } catch (e: SQLException) {
                return PhraseDatabase().phrase
            }

        }

    @Throws(ClassNotFoundException::class)
    private fun connect(): Connection? {
        Class.forName("org.sqlite.JDBC")
        return try {DriverManager.getConnection("jdbc:sqlite::resource:data/phrases.db")}
                catch (e: SQLException) {null}
    }


}

 class Phrase(var phrase: String? = null, var category: String? = null)

 class Categories(var table: String? = null, var title: String? = null)