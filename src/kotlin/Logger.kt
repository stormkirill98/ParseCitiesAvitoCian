import org.json.JSONException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    lateinit var currentCity: String
    val file = File("C:\\Users\\odmen\\Desktop\\AvitoCianParser\\src\\logs")

    fun logWrongCityId(errorId: Int, cite: String) {
        when (errorId) {
            -1 -> file.appendText("$currentCity: $cite not found location\n")
            -2 -> file.appendText("$currentCity: $cite not found name\n")
        }
    }

    fun logException(e: JSONException) {
        file.appendText("$currentCity: ${e.localizedMessage}\n")
    }

    fun logWrongTransliterateCityName(transliterateName: String, cite: String) {
        file.appendText("$currentCity: not fetch $transliterateName on $cite\n")
    }

    fun logNotEqualsDistrictsSize(avitoDistrictsSize: Int, cianDistrictsSize: Int) {
        file.appendText("$currentCity: $avitoDistrictsSize districts from avito; $cianDistrictsSize districts from cian\n")
    }

    fun logNotFoundDistrict(name: String, cite: String) {
        file.appendText("$currentCity: $name not found on $cite\n")
    }

    fun logNewRunning() {
        val formatter = SimpleDateFormat("dd-MM HH:mm")
        file.appendText("\n\n*** ${formatter.format(Date())} ***\n")
    }

    fun logWrongName(cityName: String, nameInJson: String, cite: String) {
        file.appendText("$currentCity: $cite. saving.City name: $cityName. Name in json: $nameInJson\n")
    }

    fun logNotDefineMetroId(metroName: String) {
        file.appendText("$currentCity: cian. Not define id for $metroName\n")
    }

    fun logNotFoundLogs(metroName: String) {
        file.appendText("$currentCity: cian. Not found logs with request for $metroName\n")
    }

    fun logNotClick(metroName: String) {
        file.appendText("$currentCity: cian. not click on $metroName\n")
    }

    fun logNotLoadPage() {
        file.appendText("$currentCity: not load page\n")
    }

    fun logNotDefineUrlOrId(cityName: String, cite: String) {
        file.appendText("$cityName: not define url or id on $cite\n")
    }

    fun logWrongNameOnPage(cityName: String, cityNameOnPage: String, currentUrl: String) {
        file.appendText("$cityName: on page $currentUrl name is $cityNameOnPage\n")

    }
}
