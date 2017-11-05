package org.simonscode.nanoupdater

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import java.io.*

class Config private constructor() {
    var firstRun = true
    var doumentPath = ""
    var minutesBetweenUpdates = 0
    var storeCredentials = false
    var username = ""
    var password = ""
    var wordcount = 0

    @JvmOverloads
    fun save(file: File = File(CONFIGFILE)) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonConfig = gson.toJson(this)
        val writer: FileWriter
        try {
            writer = FileWriter(file)
            writer.write(jsonConfig)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun toString(): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }

    companion object {

        // Configfile name
        private val CONFIGFILE = "config.json"
        private var instance: Config? = null

        fun get(): Config {
            if (instance == null) {
                load()
            }
            return instance!!
        }

        private fun load(file: File = File(CONFIGFILE)) {
            try {
                val gson = GsonBuilder().create()
                val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
                instance = gson.fromJson(reader, Config::class.java)
            } catch (e: FileNotFoundException) {
                if (instance == null) {
                    instance = Config()
                    instance!!.save()
                }
            } catch (e: JsonIOException) {
                System.err.println("Config file improperly formatted!")
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                System.err.println("Config file improperly formatted!")
                e.printStackTrace()
            }
        }
    }
}