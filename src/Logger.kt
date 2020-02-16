import java.io.File

object Logger {
    val file = File("C:\\Users\\odmen\\Desktop\\AvitoCianParser\\src\\logs")

    fun logWrongCityId(city: String, errorId: Int) {
        when (errorId) {
            -1 -> file.appendText("$city: not found location")
            -2 -> file.appendText("$city: not found name")
            -3 -> file.appendText("$city: name in json not equals input name")
        }
    }
}


