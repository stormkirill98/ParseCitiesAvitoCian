package cities

import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import java.io.Closeable
import java.util.concurrent.TimeUnit

class Browser : Closeable {
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