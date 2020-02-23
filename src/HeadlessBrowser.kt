import org.json.JSONException
import org.json.JSONObject
import org.openqa.selenium.By
import org.openqa.selenium.ElementClickInterceptedException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.logging.LogEntry
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.RemoteWebElement
import java.util.logging.Level


fun main() {
    HeadlessBrowser.getMetroCian("МОсква")
}


object HeadlessBrowser {
    private val driver: WebDriver
    init {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\odmen\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe")

        val logPrefs = LoggingPreferences()
        logPrefs.enable(LogType.PERFORMANCE, Level.INFO)

        val options = ChromeOptions().apply {
            setCapability("goog:loggingPrefs", logPrefs)
//            setHeadless(true)
        }

        driver = ChromeDriver(options)
    }

    fun getMetroCian(city: String): ArrayList<DistrictDto> {
        val metroList = ArrayList<DistrictDto>()
        Logger.currentCity = city // TODO delete
        val metroButtonCssClasses = "_2_I0uxAX1QTt_l4n _35LKst7i1uZi74JV _2yfeFLx02AjM6sHY _93444fe79c--button--T1QJW _93444fe79c--button--1-EOD _93444fe79c--button--first--uMIyU"

        driver.get("https://cian.ru/snyat-kvartiru/")

        var index = 0
        while (index < 350) {
            try {
                val metroBtn = driver.findElement(By.cssSelector("button[class='$metroButtonCssClasses']"))
                metroBtn.click()

                Thread.sleep(100)
                // break by exception
                while (++index < 350) {
                    val metroElement = driver
                        .findElement(By.cssSelector("div[class='underground_map_widget-metro-EWxE5zul']"))
                        .findElement(By.tagName("svg"))
                        .findElement(By.xpath(".//*[$index]"))

                    try {
                        val name = metroElement.text

                        // skip metro branch
                        if (name.length <= 3) continue

                        try {
                            val circle = (metroElement as RemoteWebElement).findElementByTagName("circle")
                            val actions = Actions(driver)
                            actions.moveToElement(circle).click().build().perform()
                        } catch (e: ElementClickInterceptedException) {
                            println(e.localizedMessage)
                        }
                        Thread.sleep(500)
                        val requestLogs = getRequestLogs()

                        if (requestLogs.isEmpty()) {
                            Logger.logNotFoundLogs(name)
                            continue
                        }

                        val ids = getIds(requestLogs.last(), metroList)
                        if (ids.isEmpty()) {
                            Logger.logNotDefineMetroId(name)
                            continue
                        }

                        for (id in ids) {
                            metroList.add(DistrictDto(name, idCian = id, isMetro = true))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: ElementClickInterceptedException) {
                val closeBtn = driver.findElement(By.cssSelector("div[class='_93444fe79c--button--3lsO-']"))
                closeBtn.click()
                Thread.sleep(200)
            }
            catch (e: Exception) {
                println(e.message)
                println()
            }
        }

        writeCityWithDistricts("Казань", metroList)
        driver.close()
        driver.quit()

        return metroList
    }

    private fun getRequestLogs(): List<LogEntry> {
        return driver.manage().logs()[LogType.PERFORMANCE].filter {
            try {
                val json = JSONObject(it.message)
                val urlInJson = json
                    .getJSONObject("message")
                    .getJSONObject("params")
                    .getJSONObject("request")
                    .getString("url")
                return@filter urlInJson == "https://www.cian.ru/cian-api/site/v1/offers/search/meta/"
            } catch (e: JSONException) { return@filter false }
        }
    }

    private fun getIds(log: LogEntry, districts: ArrayList<DistrictDto>): ArrayList<Int> {
        val ids = ArrayList<Int>()
        val json = JSONObject(log.message)

        try {
            val postData = json
                .getJSONObject("message")
                .getJSONObject("params")
                .getJSONObject("request")
                .getString("postData")

            val idElements = JSONObject(postData)
                .getJSONObject("geo")
                .getJSONArray("value")

            for(idElement in idElements) {
                if (idElement !is JSONObject) continue

                val id = idElement.getInt("id")
                if (alreadyFoundDistrict(id, districts)) continue

                ids.add(id)
            }
        } catch (e: JSONException) { e.printStackTrace() }

        return ids
    }

    private fun alreadyFoundDistrict(id: Int, districts: ArrayList<DistrictDto>): Boolean {
        for (district in districts) {
            if (district.idCian == id) return true
        }

        return false
    }
}