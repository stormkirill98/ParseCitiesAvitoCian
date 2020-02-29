import org.json.JSONArray
import org.json.JSONObject
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import java.io.Closeable
import java.io.File
import java.lang.Exception

const val FILE_NAME = "more_cities"
const val CIAN_URL = "https://yaroslavl.cian.ru/"
const val AVITO_URL = "https://www.avito.ru/yaroslavl"

const val CIAN_BUTTON_SHOW_SEARCH_CLASS = "geo-link--9bfd399c7a2845c23cfa3b226716fd27"
const val CIAN_SEARCH_INPUT_CLASS = "text_field_component-input-r5g2rpzU text_field_component-small-8egraV0q input--9c1e16c1ec4b69df81049cd40d4c9ed4"
const val CIAN_SUGGESTIONS_LIST_CLASS = "list--79e175a2c2de90a657da1f79e3a62c08"
const val CIAN_ENTER_BUTTON = "button_component-button-3KmZZLvJ button_component-S-th694M5g button_component-default-LU0A0kvh button_component-primary-2nYBoa2Y button--8da9b20b88751e792a7fc2fcb7bd90ee"

const val AVITO_BUTTON_SHOW_SEARCH_CLASS = "main-locationWrapper-3C0pT"
const val AVITO_SEARCH_INPUT_CLASS = "suggest-input-3p8yi"
const val AVITO_SUGGESTIONS_LIST_CLASS = "suggest-suggests-bMAdj"
const val AVITO_ENTER_BUTTON = "button-button-2Fo5k button-size-m-7jtw4 button-primary-1RhOG"

fun main() {
    val cities = readCities().subList(0, 10)
    val jsonArray = JSONArray()

    var index = 1
    for (cityName in cities) {
        println("Read $index. $cityName")

        val jsonCity = JSONObject().apply {
            put("id", index++)
            put("name", cityName)
        }

        jsonArray.put(jsonCity)
    }

    Browser().use {
        it.openCianPage()
        fillCities("cian_url", jsonArray, it)

        it.openAvitoPage()
        fillCities("avito_url", jsonArray, it)
    }

    writeCities(jsonArray)
}

fun fillCities(key: String, jsonArray: JSONArray, browser: Browser) {
    for (jsonObj in jsonArray) {
        if (jsonObj !is JSONObject) continue

        if (jsonObj.getString(key).isEmpty()) {
            val name = jsonObj.getString("name")
            println("Fill $key. $name")

            val url = if (key == "avito_url")
                                browser.getAvitoUrl(name)
                             else browser.getCianUrl(name)

            if (url.isEmpty()) {
                Logger.logNotDefineUrl(name, key)
                continue
            }

            jsonObj.put(key, url)
            Thread.sleep(500)
        }
    }
}

fun readCities(): List<String> {
    val file = {}.javaClass.getResource("$FILE_NAME.csv")
    val fileText = file.readText()

    return fileText.lines().map { it.substringAfter(",") }
}

fun writeCities(jsonArray: JSONArray) {
    val path = System.getProperty("user.dir") + "\\src\\resources\\$FILE_NAME.json"
    val jsonFile = File(path)
    jsonFile.createNewFile()

    val jsonObject = JSONObject()
    jsonObject.put("cities", jsonArray)

    jsonFile.appendText(jsonObject.toString(2))
}

class Browser : Closeable {
    private val driver: WebDriver

    init {
        System.setProperty(
            "webdriver.chrome.driver",
            "C:\\Users\\odmen\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe"
        )

        val options = ChromeOptions().apply {
//            setHeadless(true)
            setAcceptInsecureCerts(true)
        }

        driver = ChromeDriver(options)
    }

    override fun close() {
        driver.quit()
    }

    fun openCianPage() {
        driver.get(CIAN_URL)
    }

    fun openAvitoPage() {
        driver.get(AVITO_URL)
    }

    fun getCianUrl(cityName: String): String {
        return try {
            goToCityPage(
                cityName,
                "svg[class='$CIAN_BUTTON_SHOW_SEARCH_CLASS']",
                "input[class='$CIAN_SEARCH_INPUT_CLASS']",
                "ul[class='$CIAN_SUGGESTIONS_LIST_CLASS']",
                "button[class='$CIAN_ENTER_BUTTON']"
            )

            driver.currentUrl
        } catch (e: Exception) {
            ""
        }
    }

    fun getAvitoUrl(cityName: String): String {
        return try {
            goToCityPage(
                cityName,
                "div[class='$AVITO_BUTTON_SHOW_SEARCH_CLASS']",
                "input[class='$AVITO_SEARCH_INPUT_CLASS']",
                "ul[class='$AVITO_SUGGESTIONS_LIST_CLASS']",
                "button[class='$AVITO_ENTER_BUTTON']"
            )

            driver.currentUrl
        } catch (e: Exception) {
            ""
        }
    }

    private fun goToCityPage(
        cityName: String,
        btnShowSearchCitySelector: String,
        inputFieldSelector: String,
        suggestionListSelector: String,
        confirmButtonSelector: String
    ) {
        val btnShowSearchCity =
            driver.findElement(By.cssSelector(btnShowSearchCitySelector))
        click(btnShowSearchCity, 200)

        val inputField = driver.findElement(By.cssSelector(inputFieldSelector))
        type(cityName, inputField)

        val suggestionsListEl = driver.findElement(By.cssSelector(suggestionListSelector))
        val suggestionElements = suggestionsListEl.findElements(By.tagName("li"))
        click(suggestionElements.first())

        val sendButton = driver.findElement(By.cssSelector(confirmButtonSelector))
        click(sendButton)
    }

    private fun click(el: WebElement, timeoutMs: Long = 1000): Boolean {
        return try {
            val actions = Actions(driver)
            actions.moveToElement(el).click().build().perform()
            Thread.sleep(timeoutMs)
            true
        } catch (e: ElementClickInterceptedException) {
            false
        }
    }

    private fun type(text: String, el: WebElement) {
        Actions(driver)
            .click(el)
            .keyDown(Keys.CONTROL)
            .sendKeys("a")
            .keyUp(Keys.CONTROL)
            .sendKeys(text)
            .build()
            .perform()
        Thread.sleep(500)
    }
}