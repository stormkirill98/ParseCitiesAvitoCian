import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.net.URLEncoder
import java.nio.charset.Charset

fun main() {
    Logger.logNewRunning()
//    connectDB()
    val cities = readCities()

    for (city in cities) {
        println("Parse $city")
        Logger.currentCity = city
        checkCityName(city)
        try {
            val avitoCityId = getAvitoCityId(city)
            if (avitoCityId < 0) Logger.logWrongCityId(avitoCityId, "avito")

            val cianCityId = getCianCityId(city)
            if (cianCityId < 0) Logger.logWrongCityId(cianCityId, "cian")

            if (avitoCityId < 0 || cianCityId < 0) continue;

            val districts = getDistricts(avitoCityId, cianCityId)
            writeCityWithDistricts(city, districts)
            Thread.sleep(1000)
        } catch (e: JSONException) {
            Logger.logException(e)
        }

        Thread.sleep(1000)
    }
}

fun readCities(): List<String> {
    val file = {}.javaClass.getResource("cities.csv")
    val fileText = file.readText(Charset.forName("windows-1251"))

    return fileText.lines().map { it.substringAfter(",") }.subList(0, 10)
}

fun getAvitoCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://www.avito.ru/web/1/slocations?limit=5&q=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("result")?.getJSONArray("locations")

    val firstLocation = locations?.first() as JSONObject? ?: return -1
    val nameInJson = firstLocation.getJSONObject("names")?.getString("1") ?: return -2
    if (nameInJson != cityName) {
        Logger.logWrongName(cityName, nameInJson, "avito")
        return -3
    }

    return firstLocation.getInt("id")
}

fun getCianCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://yaroslavl.cian.ru/cian-api/site/v1/search-regions-cities/?text=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("data")?.getJSONArray("items")

    val firstLocation = locations?.first() as JSONObject? ?: return -1
    val nameInJson = firstLocation.getString("displayName") ?: return -2
    if (nameInJson != cityName) {
        Logger.logWrongName(cityName, nameInJson, "cian")
        return -3
    }

    return firstLocation.getInt("id")
}

fun getDistricts(avitoCityId: Int, cianCityId: Int): List<DistrictDto> {
    var isMetro = false

    Thread.sleep(500)
    val resAvito = try {
        fetchData("https://www.avito.ru/web/1/locations/districts?locationId=$avitoCityId")
    } catch (e: FileNotFoundException) {
        fetchData("https://www.avito.ru/web/1/locations/metro?locationId=$avitoCityId")
        isMetro = true
    }
    val avitoDistricts = parseDistrictsJson(JSONArray(resAvito), "avito")

    val cianDistricts = if (isMetro) {
        // TODO
        emptyList<DistrictDto>()
    } else {
        val resCian = fetchData("https://yaroslavl.cian.ru/api/geo/get-districts-tree/?locationId=$cianCityId")
        parseDistrictsJson(JSONArray(resCian), "cian")
    }

    return combineDistricts(avitoDistricts, cianDistricts)
}

fun parseDistrictsJson(array: JSONArray, cite: String): ArrayList<DistrictDto> {
    val districts = arrayListOf<DistrictDto>()

    for (obj in array) {
        if (obj !is JSONObject) continue

        val id = obj.getInt("id")
        val name = obj.getString("name")

        val district = if (cite == "cian")
            DistrictDto(name, idCian = id)
        else DistrictDto(name, idAvito = id)

        districts.add(district)
    }

    return districts
}

fun combineDistricts(avitoDistricts: List<DistrictDto>, cianDistricts: List<DistrictDto>): List<DistrictDto> {
    if (avitoDistricts.size != cianDistricts.size) {
        Logger.logNotEqualsDistrictsSize(avitoDistricts.size, cianDistricts.size)
    }

    val size = if (avitoDistricts.size > cianDistricts.size)
        avitoDistricts.size
    else cianDistricts.size

    val newList = ArrayList<DistrictDto>(size)

    for (i in 0 until size) {
        if (i < cianDistricts.size) {
            val districtFromCian = cianDistricts[i]
            val districtFromAvito = getDistrictByName(avitoDistricts, districtFromCian.name)

            if (districtFromAvito == null) Logger.logNotFoundDistrict(districtFromCian.name, "avito")

            districtFromAvito?.let { districtFromCian.idAvito = districtFromAvito.idAvito }
            newList.add(districtFromCian)
            continue
        }

        if (i < avitoDistricts.size) {
            val districtFromAvito = avitoDistricts[i]
            val districtFromCian = getDistrictByName(cianDistricts, districtFromAvito.name)

            if (districtFromCian == null) Logger.logNotFoundDistrict(districtFromAvito.name, "cian")

            districtFromCian?.let { districtFromAvito.idCian = districtFromCian.idCian }
            newList.add(districtFromAvito)
            continue
        }
    }

    return newList
}

fun checkCityName(name: String): Boolean {
    var result = true
    val transliterateName = transliterateCyrillicToLatin(name)

    Thread.sleep(500)
    if (!checkUrl("https://www.avito.ru/$transliterateName")) {
        Logger.logWrongTransliterateCityName(transliterateName, "avito")
        result = false
    }

    if (!checkUrl("https://$transliterateName.cian.ru/")) {
        Logger.logWrongTransliterateCityName(transliterateName, "cian")
        result = false
    }

    return result
}

fun getDistrictByName(list: List<DistrictDto>, name: String): DistrictDto? {
    for (district in list) {
        if (district.name == name) return district
    }

    return null
}

fun writeCityWithDistricts(
    cityName: String,
    districts: List<DistrictDto>
) {
    val listDistrictJson = districts.map { it.toJSON() }
    val id = transliterateCyrillicToLatin(cityName)

    val json = JSONObject()
        .put("id", id)
        .put("name", cityName)
        .put("districts", JSONArray(listDistrictJson))

    File("test.json").appendText("$json \n\n")
}