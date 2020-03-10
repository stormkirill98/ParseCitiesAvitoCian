import java.net.HttpURLConnection
import java.net.URL

enum class Site { AVITO, CIAN }

fun checkUrl(url: String) =
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        val code = connection.responseCode

        if (connection.url.toString() != url) {
            false
        } else {
            code == 200
        }
    } catch (e: Exception) {
        false
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

fun Double.formatExt(digits: Int) = "%.${digits}f".format(this)
