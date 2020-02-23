import com.ibm.icu.text.Transliterator
import org.jetbrains.exposed.sql.Database
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL

fun checkUrl(url: String) =
    try {
        URL(url).readText()
        true
    } catch (e: Exception) {
        false
    }

fun transliterateCyrillicToLatin(str: String): String {
    val toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN")
    return toLatinTrans.transliterate(str).replace("ʹ", "").replace(" ", "_").toLowerCase()
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

fun connectDB() {
    Database.connect(
        "jdbc:postgresql://35.242.227.75:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "admin"
    )
}

fun writeCityWithDistricts(
    cityName: String,
    districts: List<DistrictDto>
) {
    val listDistrictJson = districts.map { it.toJSON() }
    val id = transliterateCyrillicToLatin(cityName)

    val json = JSONObject()
        .put("id", id)
        .put("name", cityName)
        .put("districts", JSONArray(listDistrictJson))

    File("test.json").appendText("$json \n\n")
}
