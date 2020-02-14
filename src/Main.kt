import java.net.URL
import java.net.URLEncoder

fun main() {
    val cityName = URLEncoder.encode("Москва", Charsets.UTF_8.toString())
    val res = URL("https://www.avito.ru/web/1/slocations?limit=1&q=$cityName").readText()
    println(res)
}