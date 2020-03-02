package districts

import saving.DistrictDto
import Logger
import moxproxy.builders.LocalMoxProxy
import moxproxy.interfaces.MoxProxy
import org.json.JSONException
import org.json.JSONObject
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.remote.RemoteWebElement
import writeCityWithDistricts
import java.io.Closeable


fun main() {
    Browser().use { it.getMetroCian("Казань") }
}

const val PROXY_PORT = 8089

class Browser : Closeable {
    private val driver: WebDriver
    private val proxy: MoxProxy = LocalMoxProxy.builder()
        .withPort(PROXY_PORT)
        .withRecordBodies()
        .build()

    init {
        proxy.startServer()

        System.setProperty(
            "webdriver.chrome.driver",
            "C:\\Users\\odmen\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe"
        )
        val proxyOpt = Proxy()
            .setSslProxy("localhost:$PROXY_PORT")
            .setHttpProxy("localhost:$PROXY_PORT")
            .setFtpProxy("localhost:$PROXY_PORT")
            .setProxyType(Proxy.ProxyType.MANUAL)
            .setAutodetect(false)

        val options = ChromeOptions().apply {
//            setHeadless(true)
            setProxy(proxyOpt)
            setAcceptInsecureCerts(true)
        }

        driver = ChromeDriver(options)
    }

    fun getMetroCian(city: String): ArrayList<DistrictDto> {
        val metroList = ArrayList<DistrictDto>()
        Logger.currentCity = city // TODO delete
        val metroButtonCssClasses =
            "_2_I0uxAX1QTt_l4n _35LKst7i1uZi74JV _2yfeFLx02AjM6sHY _93444fe79c--button--T1QJW _93444fe79c--button--1-EOD _93444fe79c--button--first--uMIyU"

        if (!loadPage("https://kazan.cian.ru/snyat-kvartiru/")) {
            Logger.logNotLoadPage()
            return metroList
        }

        var index = 0
        while (index < 20) {
            try {
                val metroBtn = driver.findElement(By.cssSelector("button[class='$metroButtonCssClasses']"))
                click(metroBtn, 200)

                val allMetroContainer = driver
                    .findElement(By.cssSelector("div[class='underground_map_widget-metro-EWxE5zul']"))
                    .findElement(By.tagName("svg"))
                // break by exception
                while (++index < 20) {
                    val metroElement = allMetroContainer
                            .findElement(By.xpath(".//*[$index]"))

                    try {
                        val name = metroElement.text

                        // skip metro branch
                        if (name.length <= 3) continue

                        clearNetworkInfo()
                        val metroEl = (metroElement as RemoteWebElement).findElementByTagName("circle")
                        if (!click(metroEl)) {
                            Logger.logNotClick(name)
                            continue
                        }

                        val requestLogs = getRequestLogs()
                        if (requestLogs.isEmpty()) {
                            Logger.logNotFoundLogs(name)
                            continue
                        }

                        val ids = getNewIds(requestLogs.last(), metroList)
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
                click(closeBtn, 200)
            } catch (e: Exception) {
                println(e.message)
                println()
            }
        }

        writeCityWithDistricts("Казань", metroList)

        return metroList
    }

    private fun getRequestLogs(): List<JSONObject> {
        return proxy.allRequestTraffic
            .filter { it.url.endsWith("/cian-api/site/v1/offers/search/meta/") }
            .map { JSONObject(it.body) }
    }

    private fun getNewIds(requestBody: JSONObject, districts: ArrayList<DistrictDto>): ArrayList<Int> {
        val ids = ArrayList<Int>()

        try {
            val idElements = requestBody
                .getJSONObject("geo")
                .getJSONArray("value")

            for (idElement in idElements) {
                if (idElement !is JSONObject) continue

                val id = idElement.getInt("id")
                if (alreadyFoundDistrict(id, districts)) continue

                ids.add(id)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return ids
    }

    private fun alreadyFoundDistrict(id: Int, districts: ArrayList<DistrictDto>): Boolean {
        for (district in districts) {
            if (district.idCian == id) return true
        }

        return false
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

    private fun loadPage(url: String): Boolean {
        try {
            driver.get(url)
        } catch (e: java.lang.Exception) {
            return false
        }

        return true
    }

    private fun clearNetworkInfo() = proxy.clearAllSessionEntries()

    override fun close() {
        driver.close()
        driver.quit()
        proxy.stopServer()
    }
}