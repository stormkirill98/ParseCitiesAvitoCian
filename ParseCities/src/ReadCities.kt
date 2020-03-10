package cities

import Site
import formatExt
import org.json.JSONArray
import org.json.JSONObject
import readCitiesCsv
import writeCities

const val FILE_NAME = "test"

const val CIAN_URL = "https://yaroslavl.cian.ru/"
const val AVITO_URL = "https://www.avito.ru/yaroslavl"

const val CIAN_BUTTON_SHOW_SEARCH_CLASS = "c-header-top-link c-header-top-menu-item"
const val CIAN_SEARCH_INPUT_CLASS =
    "text_field_component-input-r5g2rpzU text_field_component-small-8egraV0q input--9c1e16c1ec4b69df81049cd40d4c9ed4"
const val CIAN_SUGGESTIONS_LIST_CLASS = "list--79e175a2c2de90a657da1f79e3a62c08"
const val CIAN_ENTER_BUTTON =
    "button_component-button-3KmZZLvJ button_component-S-th694M5g button_component-default-LU0A0kvh button_component-primary-2nYBoa2Y button--8da9b20b88751e792a7fc2fcb7bd90ee"
const val CIAN_CLOSE_SEARCH_CLASS = "close--b220039b0345a01d127bf4b34008b665"

const val AVITO_BUTTON_SHOW_SEARCH_CLASS = "main-locationWrapper-3C0pT"
const val AVITO_SEARCH_INPUT_CLASS = "suggest-input-3p8yi"
const val AVITO_SUGGESTIONS_LIST_CLASS = "suggest-suggests-bMAdj"
const val AVITO_ENTER_BUTTON = "button-button-2Fo5k button-size-m-7jtw4 button-primary-1RhOG"
const val AVITO_CLOSE_SEARCH_CLASS = "popup-close-2W0cr"

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