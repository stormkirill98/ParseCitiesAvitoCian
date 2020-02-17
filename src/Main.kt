import com.ibm.icu.text.Transliterator
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

fun main() {
//    connectDB()
    val cities = readCities()

    val city = "Ярославль"
    checkCityName(city)
    try {
        val avitoCityId = getAvitoCityId(city)
        if (avitoCityId < 0) Logger.logWrongCityId(city, avitoCityId, "avito")

        val cianCityId = getCianCityId(city)
        if (cianCityId < 0) Logger.logWrongCityId(city, cianCityId, "cian")

        saveDistricts(avitoCityId, cianCityId)
    } catch (e: JSONException) {
        Logger.logException(city, e)
    }

    Thread.sleep(1000)
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

fun getAvitoCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://www.avito.ru/web/1/slocations?limit=5&q=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("result")?.getJSONArray("locations")

    val firstLocation = locations?.first() as JSONObject? ?: return -1
    val nameInJson = firstLocation.getJSONObject("names")?.getString("1") ?: return -2
    if (nameInJson != cityName) return -3

    return firstLocation.getInt("id")
}

fun getCianCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://yaroslavl.cian.ru/cian-api/site/v1/search-regions-cities/?text=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("data")?.getJSONArray("items")

    val firstLocation = locations?.first() as JSONObject? ?: return -1
    val nameInJson = firstLocation.getString("displayName") ?: return -2
    if (nameInJson != cityName) return -3

    return firstLocation.getInt("id")
}

fun saveDistricts(avitoCityId: Int, cianCityId: Int) {
    // TODO: check ids that are positive

    val resAvito = try {
        fetchData("https://www.avito.ru/web/1/locations/districts?locationId=$avitoCityId")
    } catch (e: FileNotFoundException) {
        fetchData("https://www.avito.ru/web/1/locations/metro?locationId=$avitoCityId")
    }

    val resCian = fetchData("https://yaroslavl.cian.ru/api/geo/get-districts-tree/?locationId=$cianCityId")

    // TODO: combine arrays
    println(parseDistrictsArray(JSONArray(resAvito)))
    println(parseDistrictsArray(JSONArray(resCian)))
}

fun parseDistrictsArray(array: JSONArray): ArrayList<DistrictDto> {
    val districts = arrayListOf<DistrictDto>()

    for (obj in array) {
        if (obj !is JSONObject) continue

        val id = obj.getInt("id")
        val name = obj.getString("name")

        districts.add(DistrictDto(name, id))
    }

    return districts
}

fun fetchData(url: String): String {
    val connection = URL(url).openConnection()

    connection.addRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"
    )
    connection.addRequestProperty("Cookie", "foo=bar")
    connection.addRequestProperty("x-requested-with", "XMLHttpRequest")
    connection.addRequestProperty("x-source", "client-browser")
    connection.addRequestProperty("accept-language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7")

    return connection.getInputStream().readBytes().toString(Charsets.UTF_8)
}

fun saveDistrict(name: String, cityId: String, avitoId: Int = 0, cianId: Int = 0): District {
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

fun checkCityName(name: String): Boolean {
    var result = true
    val transliterateName = transliterateCyrillicToLatin(name.toLowerCase())

    if (!checkUrl("https://www.avito.ru/$transliterateName")) {
        Logger.logWrongTransliterateCityName(name, transliterateName, "avito")
        result = false
    }
    Thread.sleep(500)

    if (!checkUrl("https://$transliterateName.cian.ru/")) {
        Logger.logWrongTransliterateCityName(name, transliterateName, "cian")
        result = false
    }
    Thread.sleep(500)

    return result
}

fun checkUrl(url: String) =
    try {
        URL(url).readText()
        true
    } catch (e: FileNotFoundException) {
        false
    }

fun transliterateCyrillicToLatin(str: String): String {
    val toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN")
    return toLatinTrans.transliterate(str).replace("ʹ", "")
}