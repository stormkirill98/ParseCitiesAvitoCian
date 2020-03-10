package cities

import org.json.JSONObject

internal fun main() {
    val file = {}.javaClass.getResource("parse_cities.json")
    val fileText = file.readText()

    val jsonArray = JSONObject(fileText).getJSONArray("cities")

    val filteredArray = jsonArray.filter {
        (it as JSONObject).getString("cian_url").isEmpty()
                || it.getString("avito_url").isEmpty()
    }

    val countCianEmpty = filteredArray.count { (it as JSONObject).getString("cian_url").isEmpty() }
    val countAvitoEmpty = filteredArray.count { (it as JSONObject).getString("avito_url").isEmpty() }

    println("Count cian city not define: $countCianEmpty")
    println("Count avito city not define: $countAvitoEmpty")

    val avitoNotDefineCities = filteredArray
        .filter { (it as JSONObject).getString("avito_url").isEmpty() }
        .map { (it as JSONObject).getString("name") }

    avitoNotDefineCities.forEach(::println)
}