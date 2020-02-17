import org.json.JSONException
import java.io.File

object Logger {
    val file = File("C:\\Users\\odmen\\Desktop\\AvitoCianParser\\src\\logs")

    fun logWrongCityId(city: String, errorId: Int, cite: String) {
        when (errorId) {
            -1 -> file.appendText("$city: $cite not found location\n")
            -2 -> file.appendText("$city: $cite not found name\n")
            -3 -> file.appendText("$city: $cite name in json not equals input name\n")
        }
    }

    fun logException(city: String, e: JSONException) {
        file.appendText("$city: ${e.localizedMessage}\n")
    }

    fun logWrongTransliterateCityName(name: String, transliterateName: String, cite: String) {
        file.appendText("$name: not fetch $transliterateName on $cite\n")
    }
}


