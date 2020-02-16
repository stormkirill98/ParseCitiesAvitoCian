import com.ibm.icu.text.Transliterator
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

fun main() {
//    connectDB()
    System.setProperty("http.agent", "Chrome");
    val cities = readCities()

    // TODO city with metro?
    for (city in cities) {
        val cityId = getCityId(city)
        if (cityId < 0) Logger.logWrongCityId(city, cityId)

        getDistricts(cityId)

        Thread.sleep(1000)
    }
}

fun connectDB() {
    Database.connect(
        "jdbc:postgresql://35.242.227.75:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "admin"
    )
}

fun readCities(): List<String> {
    val file = {}.javaClass.getResource("cities.csv")
    val fileText = file.readText(Charset.forName("windows-1251"))

    return fileText.lines().map { it.substringAfter(",") }
}

fun getCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://www.avito.ru/web/1/slocations?limit=5&q=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("result")?.getJSONArray("locations")
    val firstLocation = locations?.first() as JSONObject? ?: return -1

    val nameInJson = firstLocation.getJSONArray("names")?.first() as String? ?: return -2

    if (nameInJson != cityName) return -3

    return firstLocation.getInt("id")
}

fun getDistricts(cityId: Int) {
    val res = fetchData("https://www.avito.ru/web/1/locations/districts?locationId=$cityId")
    for(obj in JSONArray(res)) {
        if (obj !is JSONObject) continue

        val id = obj.getInt("id")
        val name = obj.getString("name")


    }
}

fun fetchData(url: String): String {
    val connection = URL(url).openConnection()

    connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
    connection.addRequestProperty("Cookie", "foo=bar")
    connection.addRequestProperty("accept", "application/json")
    connection.addRequestProperty("x-requested-with", "XMLHttpRequest")
    connection.addRequestProperty("x-source", "client-browser")
    connection.addRequestProperty("accept-encoding", "gzip, deflate, br")
    connection.addRequestProperty("accept-language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7")

    return try {
        connection.getInputStream().readBytes().toString()
    } catch (e: IOException) {
        println("Retry fetch from $url")
        Thread.sleep(5000)
        connection.getInputStream().readBytes().toString()
    }
}

fun saveDistrict(name: String, cityId: String, avitoId: Int = 0, cianId: Int= 0): District {
    return transaction {
        District.new {
            this.name = name
            this.cityName = cityId
            this.idAvito = avitoId
            this.idCian = cianId
        }
    }
}

fun saveCity(name: String): City {
    val id = transliterateCyrillicToLatin(name)
    return transaction {
        City.new(id) {
            this.name = name
        }
    }
}

fun transliterateCyrillicToLatin(str: String): String {
    val toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN")
    return toLatinTrans.transliterate(str).replace("ʹ", "")
}