package cities

import Site
import formatExt
import org.json.JSONArray
import org.json.JSONObject
import readCitiesCsv
import writeCities

const val FILE_NAME = "test"

internal fun main() {
    Logger.logNewRunning()

    val cities = readCitiesCsv(FILE_NAME)
    val jsonArray = JSONArray()

    for ((i, cityName) in cities.withIndex()) {
        println("Read ${i + 1}. $cityName")

        val jsonCity = JSONObject().apply {
            put("id", i + 1)
            put("name", cityName)
        }

        jsonArray.put(jsonCity)
    }

    Browser().use {
        it.openCianPage()
        fillCities(Site.CIAN, jsonArray, it)

        it.openAvitoPage()
        fillCities(Site.AVITO, jsonArray, it)
    }


    val filteredArray =
        jsonArray.filter {
            !((it as JSONObject).getString("cian_url").isEmpty()
                    && it.getString("cian_url").isEmpty())
        }

    println("Save ${filteredArray.size}")

    writeCities(FILE_NAME, JSONArray(filteredArray))
}

internal fun fillCities(site: Site, jsonArray: JSONArray, browser: Browser) {
    for ((i, jsonObj) in jsonArray.withIndex()) {
        if (jsonObj !is JSONObject) continue

        val name = jsonObj.getString("name")
        val percent = (i + 1) / jsonArray.length().toDouble()
        println("$i Fill $site. $name       ${percent.formatExt(2)}")

        val (url, id) = if (site == Site.AVITO)
            browser.getAvitoInfo(name)
        else browser.getCianInfo(name)

        if (url.isEmpty() || id == null) {
            Logger.logNotDefineUrlOrId(name, site.name)
        }

        jsonObj.put(site.name.toLowerCase() + "_url", url)
        jsonObj.put(site.name.toLowerCase() + "_id", id)
        Thread.sleep(500)
    }
}