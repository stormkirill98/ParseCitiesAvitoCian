import org.jetbrains.exposed.sql.Database
import java.nio.charset.Charset

fun main() {
    println(readCities().toString())
//    connectDB()

    /*val cityName = URLEncoder.encode("Москва", Charsets.UTF_8.toString())
    val res = URL("https://www.avito.ru/web/1/slocations?limit=5&q=$cityName").readText()
    println(res)

    val json = JSONObject(res)
    val locations = json.getJSONObject("result")?.getJSONArray("locations")!!

    val firstLocation = locations.first() as JSONObject

    val id = firstLocation.getInt("id")*/
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

    return fileText.split("\r\n").map { it.substringAfter(",") }
}
