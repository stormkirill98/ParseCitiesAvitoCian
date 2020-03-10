package cities

import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import java.io.Closeable
import java.util.concurrent.TimeUnit

const val CIAN_URL = "https://yaroslavl.cian.ru/"
const val AVITO_URL = "https://www.avito.ru/yaroslavl"

const val CIAN_BUTTON_SHOW_SEARCH_CLASS = "c-header-top-link c-header-top-menu-item"
const val CIAN_SEARCH_INPUT_CLASS =
        "text_field_component-input-r5g2rpzU text_field_component-small-8egraV0q input--9c1e16c1ec4b69df81049cd40d4c9ed4"
const val CIAN_SUGGESTIONS_LIST_CLASS = "list--79e175a2c2de90a657da1f79e3a62c08"
const val CIAN_ENTER_BUTTON =
        "button_component-button-3KmZZLvJ button_component-S-th694M5g button_component-default-LU0A0kvh button_component-primary-2nYBoa2Y button--8da9b20b88751e792a7fc2fcb7bd90ee"
const val CIAN_CLOSE_SEARCH_CLASS = "close--b220039b0345a01d127bf4b34008b665"

const val AVITO_BUTTON_SHOW_SEARCH_CLASS = "main-locationWrapper-3C0pT"
const val AVITO_SEARCH_INPUT_CLASS = "suggest-input-3p8yi"
const val AVITO_SUGGESTIONS_LIST_CLASS = "suggest-suggests-bMAdj"
const val AVITO_ENTER_BUTTON = "button-button-2Fo5k button-size-m-7jtw4 button-primary-1RhOG"
const val AVITO_CLOSE_SEARCH_CLASS = "popup-close-2W0cr"

internal class Browser : Closeable {
    private val driver: WebDriver

    init {
        System.setProperty(
            "webdriver.chrome.driver",
            "C:\\Users\\odmen\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe"
        )

        val options = ChromeOptions().apply {
            setHeadless(false)
            setAcceptInsecureCerts(true)
        }

        driver = ChromeDriver(options)
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS)
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

    fun getCianInfo(cityName: String): Pair<String, Int?> {
        return try {
            goToCityPage(
                cityName,
                "a[class='$CIAN_BUTTON_SHOW_SEARCH_CLASS']",
                "input[class='$CIAN_SEARCH_INPUT_CLASS']",
                "ul[class='$CIAN_SUGGESTIONS_LIST_CLASS']",
                "button[class='$CIAN_ENTER_BUTTON']"
            )

            val sessionKey = driver.manage().getCookieNamed("session_region_id")
            val id = sessionKey?.value?.toIntOrNull()

            driver.currentUrl to id
        } catch (e: NoSuchElementException) {
            try {

                val closeButton = driver.findElement(By.cssSelector("svg[class='$CIAN_CLOSE_SEARCH_CLASS']"))
                click(closeButton, 0)
            } catch (e: Exception) {
            }
            "" to null
        } catch (e: Exception) {
            e.printStackTrace()
            "" to null
        }
    }

    fun getAvitoInfo(cityName: String): Pair<String, Int?> {
        return try {
            goToCityPage(
                cityName,
                "div[class='$AVITO_BUTTON_SHOW_SEARCH_CLASS']",
                "input[class='$AVITO_SEARCH_INPUT_CLASS']",
                "ul[class='$AVITO_SUGGESTIONS_LIST_CLASS']",
                "button[class='$AVITO_ENTER_BUTTON']"
            )

            val sessionKey = driver.manage().getCookieNamed("buyer_location_id")
            val id = sessionKey?.value?.toIntOrNull()

            driver.currentUrl to id
        } catch (e: NoSuchElementException) {
            try {
                val closeButton = driver.findElement(By.cssSelector("button[class='$AVITO_CLOSE_SEARCH_CLASS']"))
                click(closeButton, 0)
            } catch (e: Exception) {
            }
            "" to null
        } catch (e: Exception) {
            "" to null
        }
    }

    private fun goToCityPage(
        cityName: String,
        btnShowSearchCitySelector: String,
        inputFieldSelector: String,
        suggestionListSelector: String,
        confirmButtonSelector: String
    ) {
        val btnShowSearchCity = driver.findElement(By.cssSelector(btnShowSearchCitySelector))
        click(btnShowSearchCity, 0)

        val inputField = driver.findElement(By.cssSelector(inputFieldSelector))
        type(cityName, inputField)

        val suggestionsListEl = driver.findElement(By.cssSelector(suggestionListSelector))
        val suggestionElements = suggestionsListEl.findElements(By.tagName("li"))
        val suggestionEl = suggestionElements.first {
            it.text.substringBefore(",") == cityName || it.text.substringBefore(" (") == cityName
        }
        click(suggestionEl)

        val sendButton = driver.findElement(By.cssSelector(confirmButtonSelector))
        click(sendButton)

        try {
            val cityNameOnPage = driver.findElement(By.cssSelector(btnShowSearchCitySelector)).text
            if (cityNameOnPage != cityName) {
                Logger.logWrongNameOnPage(cityName, cityNameOnPage, driver.currentUrl)
            }
        } catch (e: Exception) {
        }
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
        val action = Actions(driver)
            .click(el)
            .keyDown(Keys.CONTROL)
            .sendKeys("a")
            .keyUp(Keys.CONTROL)
            .pause(10)
        /*.sendKeys(text)
        .build()
        .perform()*/

        for (c in text) {
            action.sendKeys(c.toString())
                .pause(100)
        }

        action.build().perform()

        Thread.sleep(500)
    }
}